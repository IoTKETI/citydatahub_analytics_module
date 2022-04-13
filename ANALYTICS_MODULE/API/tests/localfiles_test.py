# Dispatch Method Pattern(https://legacy.python.org/workshops/1997-10/proceedings/savikko.html#python:dispatch)
import os
from datetime import datetime
from shutil import rmtree

class InvalidPathContainedError(Exception):
    def __str__(self):
        return "Path couldn't contain " + "../ character"

class NotSupportedFileTypeError(Exception):
    def __str__(self):
        return "Suuported File Type is only " + "json"

class Localfiles:
    sample_num=5
    base_directory="/"

    def __init__(self, base_directory):
        self.base_directory=base_directory

        print("Localfiles 객체가 생성됨")

    def convert_date(self, timestamp):
        d = datetime.fromtimestamp(timestamp)
        return d

    def switch(self, case, path):
        if path is None or '../' in path:
            raise InvalidPathContainedError()

        path=base_directory +( "" if path.startswith("/") else "/")+path

        case_name = "case_"+str(case)
        selected_function= getattr(self, case_name)

        return selected_function(path)

    def case_get_list(self, path):
        file_list = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f)) and not f.startswith(".")]
        dir_list = [f for f in os.listdir(path) if not os.path.isfile(os.path.join(path, f)) and not f.startswith(".")]


        return file_list, dir_list


    def case_get_info(self, path):
        mtime=self.convert_date(os.path.getmtime(path))
        ctime=self.convert_date(os.path.getctime(path))
        stsize=os.path.getsize(path)

        return mtime, ctime, stsize


    def case_get_sample(self, path):

        if not path.endswith(".json"):
            raise NotSupportedFileTypeError()

        limit=self.sample_num
        samples = []

        with open(path) as f:
            while True:
                limit=limit-1
                line = f.readline()
                if limit < 0 or not line :
                    break
                samples.append(line)

        return samples

    def case_delete(self, path):
        print("delete "+path)
        if os.path.isfile(path):
            os.remove(path)
        elif not os.path.isfile(path):
            os.rmdir(path)
            # rmtree(path)  ->기본은 폴더만 지울 수 있도록 처리하였으며, 재귀적으로 파일을 지우고 싶을 시 os.rmdir(path) 대신 rmtree(path)를 지움

    def case_default(self, path):
        raise NotSupporteCommandError


if __name__ == "__main__":

    '''
    commands = ["get_list", "get_info", "get_sample", "delete", "default"]
    path="/"
   
    for command in commands:
        try:
            obj.switch(command, path)
        except ValueError :
            print(command+"는 지원하지 않는 명령어 입니다.")
        except MyError:
            print(path + "는 상위 폴더를 조회 할 수 없습니다. ('../' 문자열 사용 불가)")
 
    print("GetList Test")
   '''

    base_directory = ""
    path = "/"
    obj = Localfiles(base_directory)

    # 현재경로에 있는 파일 정보 출력
    file_list, dir_list=obj.switch("get_list", path)

    print("현재경로 : "+base_directory)

    print("파일리스트 " + path)
    for filename in file_list:
        full_filename = os.path.join(path, filename)
        print(full_filename)

        mtime, ctime, stsize = obj.switch("get_info", filename)
        print("파일정보",mtime, ctime, stsize)

        print("파일샘플 ",obj.sample_num, "개")

        samples =obj.switch("get_sample", filename)

        for sample in samples:
            print(sample)

    print("")
    print("폴더리스트 " + path)
    for filename in dir_list:
        full_filename = os.path.join(path, filename)
        print(full_filename)


    print("파일 삭제")

    try :
        filename = "parkingstreet_select_result_for_twohours_apply_2019092521.json"
        filename = "test2"
        obj.switch("delete", filename)

    except FileNotFoundError as e :
        print(e)


    try :
        path = "../"
        samples = obj.switch("get_sample", path)

    except InvalidPathContainedError as e:
        print(e)


    try:
        path = "parkingstreet_select_result_for_twohours_apply_2019092521.csv"
        obj.switch("get_sample", path)

    except NotSupportedFileTypeError as e:
        print(e)

    try:
        path = "/.csv"
        obj.switch("def", path)

    except AttributeError as e:
        print("Valid Command is"+"get_list," + "get_info,"+"get_sample,"+ "delete")
