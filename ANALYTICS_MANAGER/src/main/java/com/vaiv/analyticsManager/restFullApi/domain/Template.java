package com.vaiv.analyticsManager.restFullApi.domain;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
public class Template {
	private Integer templateId;
	private Integer ANALYSIS_TEMPLATE_SEQUENCE_PK;
	private String snapshotId;
	private String name;
	private JSONObject dataSummary;
	private String dataSummaryToString;
	private String dataStartDate;
	private String dataEndDate;
	private String startScript;
	private String moduleTestCommand;
	private String moduleTestVailidResult;
	private boolean publicFlag;
	private boolean deleteFlag;
	private String createDateTime;
	private String userId;
	private String userIdList;
	
	private String adminComment;
	private String progressState;
	private int customTemplateId;
	
}
