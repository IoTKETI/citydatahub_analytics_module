import os
import glob
import logging
import numbers
import numpy as np
import pandas as pd
from ast import literal_eval
from django.conf import settings

from ..utils.custom_decorator import where_exception
from ..data_preprocess.preprocess_base import PreprocessorBase

logger = logging.getLogger("collect_log_helper")


def _error_return_dict(error_type, error_msg):
    """
    Return common error dictionary type

        Parameters:
        -----------
             error_type (str) : type of error (eg. '4102')
             error_msg (str) : detail message of the error

        Returns:
        --------
             (dict) : common error dictionary
    """
    return dict(error_type=error_type, error_msg=error_msg)


class BatchTestResult(PreprocessorBase):
    def __init__(self, batch_service, test_data_path):
        self.batch_manager_id = batch_service['BATCH_SERVICE_SEQUENCE_PK']
        self.model_summary = batch_service['MODEL_SUMMARY']
        self.model_command = batch_service['MODEL_COMMAND']
        self.pdata_summary = batch_service['PREPROCESSED_DATA_SUMMARY']
        self.model_sandbox_pk = batch_service['MODEL_SANDBOX_SEQUENCE_FK1']
        self.trans_sandbox_pk = batch_service['PREPROCESSED_DATA_SANDBOX_SEQUENCE_FK2']

        self.nfs_dir = settings.ANALYTICS_MANAGER_NFS # /ANALYTICS_MANAGER_NFS/batchServer
        self.test_data_path = test_data_path
        self.nfs_batch_info_dir = os.path.join(self.nfs_dir, f'batchService_{self.batch_manager_id}')
        self.nfs_model_path = os.path.join(self.nfs_batch_info_dir, f'M_{self.model_sandbox_pk}.pickle')
        self.nfs_trans_path = glob.glob(
            os.path.join(self.nfs_batch_info_dir, f'T_{self.trans_sandbox_pk}_*.pickle'))

    # 모델학습에서 사용한 데이터와 테스트 데이터이 컬럼이 일치하는지 확인하는 함수
    @staticmethod
    def _check_train_columns(data_set, train_summary, target_data):
        test_data_columns = list(data_set.columns.values)
        test_data_columns.remove(target_data)
        test_data_columns.sort()
        train_data_summary = literal_eval(train_summary)
        train_data_columns = train_data_summary["model_train_columns"]
        train_data_columns.sort()
        
        if test_data_columns == train_data_columns:
            return True
        else:
            return False

    # Train Data 와 동일한 변환기로 Test Data 에 전처리를 수행하는 함수
    def _test_data_transformer(self, data_set, pdata_summary):
        test_data_columns = list(data_set.columns.values)
        train_pdata_summary = literal_eval(pdata_summary)  # str => list

        # 학습된 데이터의 전처리 정보를 읽어서 차례대로 동일하게 수행하는 코드
        for preprocess_info_dict in train_pdata_summary:
            field_name = preprocess_info_dict["field_name"]
            func_name = preprocess_info_dict["function_name"]
            file_name = preprocess_info_dict["file_name"]
            logger.info(f"[모델 배치] {func_name} applied to {field_name}")

            if field_name not in test_data_columns:
                return False
            else:
                if func_name == "DropColumns":
                    data_set = super()._drop_columns(data_set, field_name)
                else:
                    transformer = super()._load_pickle(
                        base_path=self.nfs_batch_info_dir, file_name=file_name
                    )
                    changed_field = transformer.transform(
                        data_set[field_name].values.reshape(-1, 1)
                    )
                    changed_field = super()._to_array(changed_field)

                    # transform 된 데이터와 원본 데이터 통합(NEW) - preprocess_helper.py 참고
                    if len(changed_field.shape) == 2 and changed_field.shape[1] == 1:
                        if func_name == "Normalizer":
                            logger.warning("Not working in this version!!!")
                        else:
                            data_set[field_name] = changed_field
                    elif len(changed_field.shape) == 1:  # LabelEncoder
                        data_set[field_name] = changed_field
                    else:
                        col_name = super()._new_columns(
                            field_name=field_name, after_fitted=changed_field
                        )
                        new_columns = pd.DataFrame(changed_field, columns=col_name)
                        data_set = pd.concat(
                            [data_set, new_columns], axis=1, sort=False
                        )
                        data_set = data_set.drop(field_name, axis=1)
        return data_set

    # 배치 서비스 요청에 대한 요청 파라미터 검사하는 함수
    def check_request_batch_path(self):
        check_list = [self.test_data_path, self.nfs_model_path, self.nfs_trans_path[0]]
        for check_path in check_list:
            logger.info(f"경로 확인 중... [{check_path}]")
            if not os.path.isfile(check_path):
                logger.error(f"{check_path} 경로가 존재하지 않습니다")
                return dict(error_type="4004", error_msg=check_path)
        return True

    # 예측값 또는 스코어를 출력하는 함수
    def get_batch_test_result(self):
        try:
            # 테스트 데이터 로드
            if self.test_data_path.endswith(".csv"):
                test_data = pd.read_csv(self.test_data_path)
            elif self.test_data_path.endswith(".json"):
                test_data = pd.read_json(
                    self.test_data_path, lines=True, encoding="utf-8"
                )
            logger.info(f"[모델 배치] Batch ID [{self.batch_manager_id}] Data Load!")

            # 테스트 데이터 전처리
            pdata_test = self._test_data_transformer(
                data_set=test_data, pdata_summary=self.pdata_summary
            )


            if isinstance(pdata_test, bool):  # 오류 발생시 False 반환
                logger.error(
                    f"[모델 배치 err1] Batch ID [{self.batch_manager_id}] Check Columns Name"
                )
                return _error_return_dict("4022", "Data is not suitable for the model")
            target = literal_eval(self.model_command)["train_parameters"]["y"]
            is_same_columns = self._check_train_columns(
                data_set=pdata_test,
                train_summary=self.model_summary,
                target_data=target,
            )

            if not is_same_columns:
                logger.error(
                    f"[모델 배치 err2] Batch ID [{self.batch_manager_id}] Check Columns Name"
                )
                return _error_return_dict("4022", "Data is not suitable for the model")
            # 모델 로드
            model_load = super()._load_pickle(
                base_path=self.nfs_batch_info_dir,
                file_name="M_{}.pickle".format(self.model_sandbox_pk),
            )
            logger.info(f"[모델 배치] Batch ID [{self.batch_manager_id}] Model Load!")

            # 모델 테스트 결과9
            X_ = super()._drop_columns(pdata_test, target)
            y_ = np.array(pdata_test[target]).reshape(-1, 1)
            score_ = model_load.score(X=X_, y=y_)
            predict_ = model_load.predict(X=X_)
            logger.info(
                f"[모델 배치] Batch ID [{self.batch_manager_id}] Predict Result Return!"
            )

            if isinstance(predict_[0], numbers.Integral):
                result_response = {"score": "%.3f" % score_, "predict": predict_}
                return result_response
            else:
                result_response = ["%.3f" % elem for elem in predict_]
                result_response = {"score": "%.3f" % score_, "predict": result_response}
                return result_response
        except Exception as e:
            where_exception(error_msg=e)
