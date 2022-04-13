package com.vaiv.restFull.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import com.vaiv.config.ApplicationContextProvider;
import com.vaiv.restFull.domain.BatchServiceState;
import com.vaiv.restFull.domain.LogBatch;
import com.vaiv.restFull.mapper.BatchMapper;

public class BatchLogService {


    private BatchMapper batchMapper=null;

    private static Logger logger = LoggerFactory.getLogger(BatchService.class);

    private final String userId="AnalyticsBatchManager";
    private final String CODE_BATCH_START="A005001";
    private final String CODE_FILE_CHECK="A005002";
    private final String CODE_COMMAND_EXECUTE="A005003";
    private final String CODE_RESULT_BATCH="A005004";
    private final String CODE_RESULT_TRANSMIT="A005005";
    private final String CODE_BATCH_END="A005006";

    private Long batchInstanceSequence;
    private Long batchServiceSequence;
    private String storeMethod;
    private String updateAttribute;
    private String resultUpdateMethod;
    private Boolean isBatchSuccess=false;
    private Boolean isTransmitSuccess=false;
    private String batchTargetFilePath=null;
    private Long batchTargetFileSize=null;


    private LogBatch logBatch=null;

    public BatchLogService() {
        this.logBatch=new LogBatch();
        this.logBatch.setLogBatchGroupId(LocalDateTime.now());
        this.batchMapper= ApplicationContextProvider.getBean(BatchMapper.class);
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005001', '배치관리서버(스케줄링) 배치명령시작', '배치관리서버가 배치를 시작한다.');
    public void writeLogAboutBatchStart(LocalDateTime batchStartDatetime, String storeMethod, String updateAttribute, String resultUpdateMethod, String executionCycle){

        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());

        this.logBatch.setUserId(this.userId);
        this.logBatch.setCode(this.CODE_BATCH_START);

        this.logBatch.setBatchServiceSequenceFk3(this.batchServiceSequence);
        this.logBatch.setBatchInstanceSequenceFk1(this.batchInstanceSequence);

        this.logBatch.setStoreMethod(storeMethod);
        this.logBatch.setUpdateAttribute(updateAttribute);
        this.logBatch.setResultUpdateMethod(resultUpdateMethod);
        this.logBatch.setExecutionCycle(executionCycle);

        this.storeMethod=storeMethod;
        this.updateAttribute=updateAttribute;
        this.resultUpdateMethod=resultUpdateMethod;

        this.logBatch.setBatchIsSuccess(true);

        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }

        this.logBatch.setStoreMethod(null);
        this.logBatch.setUpdateAttribute(null);
        this.logBatch.setResultUpdateMethod(null);
        this.logBatch.setExecutionCycle(null);
        this.logBatch.setBatchInstanceSequenceFk1(null);
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005006', '배치관리서버(스케줄링) 배치명령종료', '배치관리서버가 배치를 완료한다.');
    public void writeLogAboutBatchEnd(LocalDateTime batchStartDatetime){

        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());
        this.logBatch.setCode(this.CODE_BATCH_END);
        this.logBatch.setBatchIsSuccess(isBatchSuccess&&isTransmitSuccess);


        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005002', '배치관리서버(스케줄링) 원본데이터 파일 확인', '배치관리서버가 원본데이터 파일을 확인한다.');
    public void writeLogAboutFileCheck(LocalDateTime batchStartDatetime,  String batchTargetFilePath, Long batchTargetFileSize, Boolean isSuccess, String batchFailReason){

        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());

        this.logBatch.setCode(this.CODE_FILE_CHECK);
        this.logBatch.setBatchTargetFilePath(batchTargetFilePath);
        this.logBatch.setBatchTargetFileSize(batchTargetFileSize);

        this.logBatch.setBatchIsSuccess(true);

        this.batchTargetFilePath=batchTargetFilePath;
        this.batchTargetFileSize=batchTargetFileSize;

        if(!isSuccess){
            this.logBatch.setBatchFailReason(batchFailReason);
            this.logBatch.setBatchIsSuccess(false);
        }

        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }
        this.logBatch.setBatchTargetFilePath(null);
        this.logBatch.setBatchTargetFileSize(null);
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005003', '배치관리서버(스케줄링) 배치모듈 명령 실행', '배치관리서버가 배치모듈 명령을 실행한다.');
    public void writeLogAboutBatchExecute(LocalDateTime batchStartDatetime,  Boolean isSuccess, String batchFailReason){

        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());

        this.logBatch.setCode(this.CODE_COMMAND_EXECUTE);
        this.logBatch.setBatchInstanceSequenceFk1(this.batchInstanceSequence);
        this.logBatch.setBatchTargetFilePath(this.batchTargetFilePath);
        this.logBatch.setBatchTargetFileSize(this.batchTargetFileSize);

        this.logBatch.setBatchIsSuccess(true);

        if(!isSuccess){
            this.logBatch.setBatchFailReason(batchFailReason);
            this.logBatch.setBatchIsSuccess(false);
        }

        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }
        this.logBatch.setBatchInstanceSequenceFk1(null);
        this.logBatch.setBatchTargetFilePath(null);
        this.logBatch.setBatchTargetFileSize(null);
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005004', '배치관리서버(스케줄링) 배치모듈 명령 결과', '배치관리서버가 배치모듈 실행 결과를 확인한다.');
    public void writeLogAboutBatchExecuteResult(LocalDateTime batchStartDatetime,  Boolean isSuccess, String batchFailReason, String batchResult ){

        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());

        this.logBatch.setCode(this.CODE_RESULT_BATCH);
        this.logBatch.setBatchIsSuccess(true);
        this.logBatch.setBatchResult(batchResult);
        this.logBatch.setBatchInstanceSequenceFk1(this.batchInstanceSequence);
        this.logBatch.setBatchTargetFilePath(this.batchTargetFilePath);
        this.logBatch.setBatchTargetFileSize(this.batchTargetFileSize);

        if(!isSuccess){
            this.logBatch.setBatchIsSuccess(false);
            this.logBatch.setBatchFailReason(batchFailReason);
        }else{

            isBatchSuccess=true;

        }

        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }
        this.logBatch.setBatchIsSuccess(null);
        this.logBatch.setBatchResult(null);
        this.logBatch.setBatchInstanceSequenceFk1(null);
        this.logBatch.setBatchTargetFilePath(null);
        this.logBatch.setBatchTargetFileSize(null);
    }

    //INSERT INTO "CODE" ("CODE",  "CODE_NAME", "DESCRIPTION") VALUES ('A005005', '배치관리서버(스케줄링) 배치코어모듈 결과 저장', '배치관리서버가 배치코어모듈로 저장 결과를 확인한다. ');
    public void writeLogAboutBatchTransferResult(LocalDateTime batchStartDatetime, Boolean isSuccess, String transferFailReason, String transferData, String transferUrl){


        this.logBatch.setBatchStartDatetime(batchStartDatetime);
        this.logBatch.setBatchEndDatetime(LocalDateTime.now());

        this.logBatch.setCode(this.CODE_RESULT_TRANSMIT);
        this.logBatch.setBatchIsSuccess(true);

        this.logBatch.setTransferData(transferData);
        this.logBatch.setTransferUrl(transferUrl);
        this.logBatch.setStoreMethod(this.storeMethod);
        this.logBatch.setUpdateAttribute(this.updateAttribute);
        this.logBatch.setResultUpdateMethod(this.resultUpdateMethod);


        if(!isSuccess){
            this.logBatch.setBatchIsSuccess(false);
            isTransmitSuccess=false;
        }else {
            if(isTransmitSuccess==null){
                isTransmitSuccess=true;
            }
        }

        try {
            this.batchMapper.insertLogBatch(this.logBatch);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("배치 기록에 실패하였습니다.");
        }

        this.logBatch.setTransferData(null);
        this.logBatch.setTransferUrl(null);
        this.logBatch.setStoreMethod(null);
        this.logBatch.setUpdateAttribute(null);
        this.logBatch.setResultUpdateMethod(null);
    }

    public void setBatchInstanceSequence(Long batchInstanceSequence) {
        this.batchInstanceSequence = batchInstanceSequence;
    }


    public void setBatchServiceSequence(Long batchServiceSequence) {
        this.batchServiceSequence = batchServiceSequence;
    }
}
