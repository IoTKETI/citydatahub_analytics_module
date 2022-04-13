package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Data
public class TemplateGw {
	private Integer templateId;
	private String snapshotId;
	private String name;
	private JSONArray entities;
	private JSONObject dataDomain;
	private String dataSummaryToString;
	private String dataStartDate;
	private String dataEndDate;
	private String startScript;
	private String moduleTestCommand;
	private String moduleTestVailidResult;
	private boolean isPublic;
	private boolean isDeleted;
	private String createDateTime;
	private String[] users;
	private String userId;
	private String userIdList;
	
	private String adminComment;
	private String progressState;
	private int customTemplateId;


	public boolean isPublic() {
		return this.isPublic;
	}

}
