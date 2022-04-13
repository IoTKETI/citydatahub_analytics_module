import logging
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from django.shortcuts import get_object_or_404

from ..models.original_data import OriginalData
from ..models.preprocessed_data import PreprocessedData
from ..models.train_info import TrainInfo
from ..models.batch_service import BatchService

from ..serializers.serializers import BatchServiceSerializer
from ..services.utils.custom_response import CustomErrorCode
from ..services.model_batch.batch_helper import BatchTestResult

logger = logging.getLogger("collect_log_view")
error_code = CustomErrorCode()


class BatchInfoView(APIView):
    def get(self, request):
        model_id = request.query_params.get("model_id")
        # model_id에 해당하는 모델의 정보 반환
        get_t = get_object_or_404(TrainInfo, pk=model_id)

        # 모델 리소스의 DELETE_FLAG  True 인 경우 409 에러 반환
        if get_t.DELETE_FLAG:
            return Response(
                error_code.CONFLICT_4009(mode="BATCH", error_msg="deleted"),
                status=status.HTTP_409_CONFLICT,
            )
        # 모델이 학습 종료되지 않은 경우도 409 에러 반환
        if get_t.PROGRESS_STATE != "success":
            return Response(
                error_code.CONFLICT_4009(mode="BATCH", error_msg=get_t.PROGRESS_STATE),
                status=status.HTTP_409_CONFLICT,
            )
        get_o = get_object_or_404(OriginalData, pk=get_t.ORIGINAL_DATA_SEQUENCE_FK1)
        get_p = get_object_or_404(
            PreprocessedData, pk=get_t.PREPROCESSED_DATA_SEQUENCE_FK2
        )

        batch_info = dict(
            MODEL_COMMAND=get_t.COMMAND,
            MODEL_SUMMARY=get_t.TRAIN_SUMMARY,
            PREPROCESSED_DATA_COMMAND=get_p.COMMAND,
            PREPROCESSED_DATA_SUMMARY=get_p.SUMMARY,
            ORIGINAL_DATA_NAME=get_o.NAME,
            MODEL_SEQUENCE_FK1=get_t.MODEL_SEQUENCE_PK,
            PREPROCESSED_DATA_SEQUENCE_FK2=get_p.PREPROCESSED_DATA_SEQUENCE_PK,
            ORIGINAL_DATA_SEQUENCE_FK3=get_o.ORIGINAL_DATA_SEQUENCE_PK,
        )
        return Response(batch_info, status=status.HTTP_200_OK)


class BatchServiceView(APIView):
    def post(self, request):
        request_data = request.data

        # TODO request_data 검사하는 코드 추가!!

        # 배치용서버에서 배치관리서버가 관리하는 Batch Info를 Batch Service로 저장하기 위한 기능
        batch_service = BatchService.objects.create(
            BATCH_SERVICE_SEQUENCE_PK=request_data["BATCH_SERVICE_SEQUENCE_PK"],
            MODEL_COMMAND=request_data["MODEL_COMMAND"],
            MODEL_SUMMARY=request_data["MODEL_SUMMARY"],
            PREPROCESSED_DATA_COMMAND=request_data["PREPROCESSED_DATA_COMMAND"],
            PREPROCESSED_DATA_SUMMARY=request_data["PREPROCESSED_DATA_SUMMARY"],
            ORIGINAL_DATA_NAME=request_data["ORIGINAL_DATA_NAME"],
            MODEL_SANDBOX_SEQUENCE_FK1=request_data["MODEL_SEQUENCE_FK1"],
            PREPROCESSED_DATA_SANDBOX_SEQUENCE_FK2=request_data[
                "PREPROCESSED_DATA_SEQUENCE_FK2"
            ],
            ORIGINAL_DATA_SANDBOX_SEQUENCE_FK3=request_data[
                "ORIGINAL_DATA_SEQUENCE_FK3"
            ],
        )
        serializers = BatchServiceSerializer(batch_service)
        return Response("batch_service_create_success", status=status.HTTP_201_CREATED)

    def get(self, request):
        queryset = BatchService.objects.all().order_by("BATCH_SERVICE_SEQUENCE_PK")
        serializer = BatchServiceSerializer(queryset, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class BatchServiceDetailView(APIView):
    # 배치용서버에서 Batch Service로 등록된 모델에 대한 테스트 결과를 Return하는 기능
    def patch(self, request, pk):
        batch_service = BatchServiceSerializer(
            get_object_or_404(BatchService, pk=pk)
        ).data
        user_request = request.data
        if "test_data_path" not in user_request.keys():
            return Response(
                error_code.MANDATORY_PARAMETER_MISSING_4101("test_data_path"),
                status=status.HTTP_400_BAD_REQUEST,
            )
        else:
            test_data_path = user_request["test_data_path"]  # 테스트 데이터의 전체 경로

            logger.info(f"요청한 배치 서비스 ID [{pk}]의 요청 정보를 확인합니다")
            batch_test_result = BatchTestResult(
                batch_service=batch_service, test_data_path=test_data_path
            )
            check_result = batch_test_result.check_request_batch_path()

            if isinstance(check_result, dict):  # check_result 타입이 dict이면 에러 메시지를 반환한 것!
                error_type = check_result["error_type"]
                error_msg = check_result["error_msg"]
                if error_type == "4004":
                    return Response(
                        error_code.FILE_NOT_FOUND_4004(path_info=error_msg),
                        status=status.HTTP_404_NOT_FOUND,
                    )
            # 예측 결과 출력
            logger.info(f"요청한 배치 서비스 ID [{pk}]의 테스트를 시작합니다")
            batch_test_result = batch_test_result.get_batch_test_result()

            if "predict" not in batch_test_result.keys():
                return Response(
                    error_code.UNPROCESSABLE_ENTITY_4022(
                        error_msg=batch_test_result["detail"]
                    ),
                    status=status.HTTP_422_UNPROCESSABLE_ENTITY,
                )
            return Response(batch_test_result, status=status.HTTP_200_OK)
