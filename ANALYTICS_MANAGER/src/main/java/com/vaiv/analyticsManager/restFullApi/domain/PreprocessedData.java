package com.vaiv.analyticsManager.restFullApi.domain;

import lombok.Data;

@Data
public class PreprocessedData {
	private Integer preprocessedDataSequencePk;
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
	private Integer originalDataSequenceFk1;
	private Integer instanceSequenceFk2;
	private String columns;
	private String statistics;
	private String sampleData;
	private Integer amount;

}
