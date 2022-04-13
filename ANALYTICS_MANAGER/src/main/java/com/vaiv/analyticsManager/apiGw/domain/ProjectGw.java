package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;

@Data
public class ProjectGw {
	private Integer projectSequencePk;
	private String name;
	private String description;
	private String createDatetime;
	private String userId;
	private Integer selectedInstance;
	private Integer instanceId;
	private boolean deleteFlag;

}
