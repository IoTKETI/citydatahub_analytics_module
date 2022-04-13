package com.vaiv.restFull.domain;

import java.time.LocalDateTime;

public class LogBatch {

	private Long logBatchSequencePk;
	private LocalDateTime logBatchGroupId;
	private String code;
	private Long batchInstanceSequenceFk1;
	private String userId;
	private Long batchServiceRequestSequenceFk2;
	private Long batchServiceSequenceFk3;
	private String batchTargetFilePath;
	private Long batchTargetFileSize;
	private LocalDateTime batchStartDatetime;
	private LocalDateTime batchEndDatetime;
	private Boolean batchIsSuccess;
	private String batchFailReason;
	private LocalDateTime createDatetime;
	private LocalDateTime modifyDatetime;
	private String batchResult;
	private String storeMethod;
	private String updateAttribute;
	private String transferData;
	private String transferUrl;
	private String resultUpdateMethod;
	private String executionCycle;


	public Long getLogBatchSequencePk() {
		return logBatchSequencePk;
	}

	public void setLogBatchSequencePk(Long logBatchSequencePk) {
		this.logBatchSequencePk = logBatchSequencePk;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getBatchInstanceSequenceFk1() {
		return batchInstanceSequenceFk1;
	}

	public void setBatchInstanceSequenceFk1(Long batchInstanceSequenceFk1) {
		this.batchInstanceSequenceFk1 = batchInstanceSequenceFk1;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getBatchServiceRequestSequenceFk2() {
		return batchServiceRequestSequenceFk2;
	}

	public void setBatchServiceRequestSequenceFk2(Long batchServiceRequestSequenceFk2) {
		this.batchServiceRequestSequenceFk2 = batchServiceRequestSequenceFk2;
	}

	public Long getBatchServiceSequenceFk3() {
		return batchServiceSequenceFk3;
	}

	public void setBatchServiceSequenceFk3(Long batchServiceSequenceFk3) {
		this.batchServiceSequenceFk3 = batchServiceSequenceFk3;
	}

	public String getBatchTargetFilePath() {
		return batchTargetFilePath;
	}

	public void setBatchTargetFilePath(String batchTargetFilePath) {
		this.batchTargetFilePath = batchTargetFilePath;
	}

	public Long getBatchTargetFileSize() {
		return batchTargetFileSize;
	}

	public void setBatchTargetFileSize(Long batchTargetFileSize) {
		this.batchTargetFileSize = batchTargetFileSize;
	}

	public LocalDateTime getBatchStartDatetime() {
		return batchStartDatetime;
	}

	public void setBatchStartDatetime(LocalDateTime batchStartDatetime) {
		this.batchStartDatetime = batchStartDatetime;
	}

	public LocalDateTime getBatchEndDatetime() {
		return batchEndDatetime;
	}

	public void setBatchEndDatetime(LocalDateTime batchEndDatetime) {
		this.batchEndDatetime = batchEndDatetime;
	}

	public Boolean getBatchIsSuccess() {
		return batchIsSuccess;
	}

	public void setBatchIsSuccess(Boolean batchIsSuccess) {
		this.batchIsSuccess = batchIsSuccess;
	}

	public String getBatchFailReason() {
		return batchFailReason;
	}

	public void setBatchFailReason(String batchFailReason) {
		this.batchFailReason = batchFailReason;
	}

	public LocalDateTime getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(LocalDateTime createDatetime) {
		this.createDatetime = createDatetime;
	}

	public LocalDateTime getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(LocalDateTime modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	public String getBatchResult() {
		return batchResult;
	}

	public void setBatchResult(String batchResult) {
		this.batchResult = batchResult;
	}

	public String getStoreMethod() {
		return storeMethod;
	}

	public void setStoreMethod(String storeMethod) {
		this.storeMethod = storeMethod;
	}

	public String getUpdateAttribute() {
		return updateAttribute;
	}

	public void setUpdateAttribute(String updateAttribute) {
		this.updateAttribute = updateAttribute;
	}

	public String getTransferData() {
		return transferData;
	}

	public void setTransferData(String transferData) {
		this.transferData = transferData;
	}

	public String getTransferUrl() {
		return transferUrl;
	}

	public void setTransferUrl(String transferUrl) {
		this.transferUrl = transferUrl;
	}

	public String getResultUpdateMethod() {
		return resultUpdateMethod;
	}

	public void setResultUpdateMethod(String resultUpdateMethod) {
		this.resultUpdateMethod = resultUpdateMethod;
	}

	public String getExecutionCycle() {
		return executionCycle;
	}

	public void setExecutionCycle(String executionCycle) {
		this.executionCycle = executionCycle;
	}

	public LocalDateTime getLogBatchGroupId() {
		return logBatchGroupId;
	}

	public void setLogBatchGroupId(LocalDateTime logBatchGroupId) {
		this.logBatchGroupId = logBatchGroupId;
	}

	@Override
	public String toString() {
		return "LogBatch{" +
				"logBatchSequencePk=" + logBatchSequencePk +
				", logBatchGroupId=" + logBatchGroupId +
				", code='" + code + '\'' +
				", batchInstanceSequenceFk1=" + batchInstanceSequenceFk1 +
				", userId='" + userId + '\'' +
				", batchServiceRequestSequenceFk2=" + batchServiceRequestSequenceFk2 +
				", batchServiceSequenceFk3=" + batchServiceSequenceFk3 +
				", batchTargetFilePath='" + batchTargetFilePath + '\'' +
				", batchTargetFileSize=" + batchTargetFileSize +
				", batchStartDatetime=" + batchStartDatetime +
				", batchEndDatetime=" + batchEndDatetime +
				", batchIsSuccess=" + batchIsSuccess +
				", batchFailReason='" + batchFailReason + '\'' +
				", createDatetime=" + createDatetime +
				", modifyDatetime=" + modifyDatetime +
				", batchResult='" + batchResult + '\'' +
				", storeMethod='" + storeMethod + '\'' +
				", updateAttribute='" + updateAttribute + '\'' +
				", transferData='" + transferData + '\'' +
				", transferUrl='" + transferUrl + '\'' +
				", resultUpdateMethod='" + resultUpdateMethod + '\'' +
				", executionCycle='" + executionCycle + '\'' +
				'}';
	}
}
