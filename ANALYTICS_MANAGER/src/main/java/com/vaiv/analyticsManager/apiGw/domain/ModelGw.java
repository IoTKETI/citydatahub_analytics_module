package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;

@Data
public class ModelGw {
	private Integer modelId;
	private String command;
	private String name;
	private String filepath;
	private String filename;
	private Integer filesize;
	private String trainSummary;
	private String validationSummary;
	private String createDatetime;
	private String progressState;
	private String progressStartDatetime;
	private String progressEndDatetime;
	private String loadState;
	private String loadProcessId;
	private boolean deleteFlag;
	private Integer originalDataId;
	private Integer preprocessedDataId;
	private Integer instanceId;
	private Integer projectId;

}
