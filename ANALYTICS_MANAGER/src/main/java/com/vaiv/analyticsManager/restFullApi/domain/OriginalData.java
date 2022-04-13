package com.vaiv.analyticsManager.restFullApi.domain;

import lombok.Data;

@Data
public class OriginalData {
	private Integer originalDataSequencePk;
	private String name;
	private String filepath;
	private String filename;
	private String extension;
	private String createDatetime;
	private boolean deleteFlag;
	private Integer projectSequenceFk1;
	private Integer instanceSequenceFk2;
	private String columns;
	private String statistics;
	private String sampleData;
	private Integer amount;


}
