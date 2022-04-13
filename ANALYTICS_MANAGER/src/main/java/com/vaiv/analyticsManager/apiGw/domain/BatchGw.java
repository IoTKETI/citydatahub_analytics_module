package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;

@Data
public class BatchGw {
	
	/* BATCH_SERVICE_REQUEST */
	private Integer batchServiceRequestId; // ==> batchServiceRequestSequencePk
	private String name;
	private Integer modelId; // ==> modelSequenceFk1
	private Integer instanceId; // ==> instanceSequenceFk2
	private Integer projectId; // ==> projectSequenceFk3
	private String dataFlowName; // ==> nifiTemplateName
	private String targetEntityId; // ==> resultUpdateDomainId
	private String targetEntityName; // ==> resultUpdateDomainName
	private String executionCycle;
	private String resultUpdateMethod;
	private String userRequestTerm;
	private String progressState;
	private String rejectReason; // ==> managerRejectReason
	private String createDatetime;
	private String modifyDatetime;
	private String userId;
	private boolean deleteFlag;
	
	private String denominatorFieldName; // ===> totalSpotNumber
	private boolean isReverseIndex;
	private String dataEntityFieldName; // ==> domainIdColumnName
	private String postprocessingType; // ==> storeMethod
	private String targetAttributeName; // ==> updateAttribute

	private String makeDataMethod;
	private String sql;
	private String targetType;
	private String datasetId;

	/* BATCH_SERVICE */
	private Integer batchServiceId; // ==> batchServiceSequencePk
	private Integer sandboxInstanceId; // ==> sandboxInstanceSequenceFk1
	private Integer batchInstanceId; // ==> batchInstanceSequenceFk2
	private String applyDataPath;
	private String applyDataNameRule;
	private String description; // ==> enrollmentTerm
	private String managerId; // ==> enrollementId
	private boolean isRunning; // ==> useFlag
	private String batchState;


	public boolean isRunning(){
		return this.isRunning;
	}

}
