package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;

@Data
public class PreprocessedDataGw {
	private Integer preprocessedDataId;
	private String command;
	private String name;
	private String filepath;
	private String filename;
	private String summary;
	private String createDatetime;
	private String progressState;
	private String progressStartDatetime;
	private String progressEndDatetime;
	private boolean deleteFlag;
	private Integer originalDataId;
	private Integer instanceId;
	private String columns;
	private String statistics;
	private String sampleData;
	private Integer amount;

}
