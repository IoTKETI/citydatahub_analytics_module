package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
public class InstanceGw {
	private Integer instanceId;
	private String name;
	private String keypairName;
	private String serverState;
	private String moduleState;
	private String privateIp;
	private String publicIp;
	private String availabilityZone;
	private String createDateTime;
	private boolean cloudInstanceGeneratedFlag;
	private String cloudInstanceFailedMessage;
	private String cloudInstanceServerId;
	private boolean deleteFlag;
	private String analysisTemplateSequenceFk1;
	private String cloudInstanceId;
	private String userId;
	private String analysisInstanceServerType;
	
	private JSONObject dataSummary;
	private String dataSummaryToString;
	private String dataStartDate;
	private String dataEndDate;
	private String startScript;
	private String moduleTestCommand;
	private String moduleTestVailidResult;
	private String snapshotId;
	
	private int templateId;
}
