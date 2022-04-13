######################[배치서버용에서 배치 요청 정보를 db에 저장하는 모델]#######################
from django.db import models

class BatchService(models.Model):
    BATCH_SERVICE_SEQUENCE_PK=models.BigIntegerField(primary_key=True)
    MODEL_COMMAND=models.TextField()
    MODEL_SUMMARY=models.TextField()
    PREPROCESSED_DATA_COMMAND=models.TextField()
    PREPROCESSED_DATA_SUMMARY=models.TextField()
    ORIGINAL_DATA_NAME=models.CharField(max_length=100)
    MODEL_SANDBOX_SEQUENCE_FK1=models.BigIntegerField()
    PREPROCESSED_DATA_SANDBOX_SEQUENCE_FK2=models.BigIntegerField()
    ORIGINAL_DATA_SANDBOX_SEQUENCE_FK3=models.BigIntegerField()
    CREATE_DATETIME=models.DateTimeField(auto_now_add=True)

    class Meta:
        managed = True
        db_table = 'BATCH_SERVICE'