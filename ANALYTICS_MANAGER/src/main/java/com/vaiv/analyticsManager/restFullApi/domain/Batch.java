package com.vaiv.analyticsManager.restFullApi.domain;

import lombok.Data;

@Data
public class Batch {
	
	/* BATCH_SERVICE_REQUEST */
	private Integer batchServiceRequestSequencePk;
	private String name;
	private Integer modelSequenceFk1;
	private Integer instanceSequenceFk2;
	private Integer projectSequenceFk3;
	private String nifiTemplateName;
	private String resultUpdateDomainId;
	private String resultUpdateDomainName;
	private String executionCycle;
	private String resultUpdateMethod;
	private String userRequestTerm;
	private String progressState;
	private String managerRejectReason;
	private String createDatetime;
	private String modifyDatetime;
	private String userId;
	private boolean deleteFlag;
	
	private String totalColumnName;
	private String domainIdColumnName;
	private String storeMethod;
	private String updateAttribute;


	private String makeDataMethod;
	private String sql;
	private String targetType;
	private String datasetId;


	/* BATCH_SERVICE */
	private Integer batchServiceSequencePk;
	private Integer sandboxInstanceSequenceFk1;
	private Integer batchInstanceSequenceFk2;
	private Integer modelSequenceFk4;
	private String applyDataPath;
	private String applyDataNameRule;
	private String enrollmentTerm;
	private String enrollementId;
	private boolean useFlag;
	private String batchState;
	

}
