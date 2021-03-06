###############[로컬 파일 리스트]#################
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from django.conf import settings

import os

import logging

from ..services.utils.custom_response import CustomErrorCode

logger = logging.getLogger('collect_log_view')
error_code = CustomErrorCode()


from ..services.file_search.localfiles import Localfiles,InvalidPathContainedError,NotSupportedFileTypeError,NotSupporteCommandError

class localfile(APIView):
    def get(self, request):

        try:
            path = request.GET['path']
        except :
            return Response(error_code.MANDATORY_PARAMETER_MISSING_4101(
                error_msg="path"),
                status=status.HTTP_400_BAD_REQUEST)
        try:
            command= request.GET['command']
        except :
            return Response(error_code.MANDATORY_PARAMETER_MISSING_4101(
                error_msg="command"),
                status=status.HTTP_400_BAD_REQUEST)

        base_directory = settings.NIFI_RESULT_DIRECTORY
        response = {}


        try:
            obj = Localfiles(base_directory)
            response['command']=command
            response['path']=path
            response['result']={}
            if command=="get_list":
                file_list, dir_list = obj.switch(command, path)
                file_infos = {}
                for filename in file_list:
                    full_filename = os.path.join(path, filename)
                    mtime, ctime, stsize = obj.switch("get_info", full_filename)

                    file_info={}
                    file_info["modifiedAt"]=mtime
                    file_info["createdAt"] = ctime
                    file_info["filesize"] = stsize
                    file_infos[filename]=file_info

                response['result']['file_list'] = file_list
                response['result']['dir_list'] = dir_list
                response['result']['file_infos'] = file_infos
            elif command=="get_sample":
                samples = obj.switch("get_sample", path)
                response['result']['samples'] = samples
            else :
                raise NotSupporteCommandError
        except FileNotFoundError as e:
            return Response(error_code.FILE_NOT_FOUND_4004(
                path_info=path),
                status=status.HTTP_404_NOT_FOUND)
        except NotADirectoryError as e:
            return Response(error_code.UNSUPPORTED_MEDIA_TYPE_4015(
                error_msg=path),
                status=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE)
        except InvalidPathContainedError as e:
            return Response(error_code.UNPROCESSABLE_ENTITY_4022(
                error_msg=("'"+path+"' is not a valid path. (../ not allowed in path)")),
                status=status.HTTP_422_UNPROCESSABLE_ENTITY)
        except NotSupportedFileTypeError as e:
            return Response(error_code.UNSUPPORTED_MEDIA_TYPE_4015(
                error_msg=path),
                status=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE)
        except AttributeError as e:
            return Response(error_code.UNPROCESSABLE_ENTITY_4022(
                error_msg=("'"+path+"' is not a valid command.")),
                status=status.HTTP_422_UNPROCESSABLE_ENTITY)
        except NotSupporteCommandError as e:
            return Response(error_code.UNPROCESSABLE_ENTITY_4022(
                error_msg=("'"+command + "' is not a valid command.")),
                status=status.HTTP_422_UNPROCESSABLE_ENTITY)
        return Response(response, status=status.HTTP_200_OK)
