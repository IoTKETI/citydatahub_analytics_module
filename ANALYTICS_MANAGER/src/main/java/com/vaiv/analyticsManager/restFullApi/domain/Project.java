package com.vaiv.analyticsManager.restFullApi.domain;

import lombok.Data;

@Data
public class Project {
	private Integer projectSequencePk;
	private String name;
	private String description;
	private String createDatetime;
	private String userId;
	private Integer selectedInstance;
	private boolean deleteFlag;

}
