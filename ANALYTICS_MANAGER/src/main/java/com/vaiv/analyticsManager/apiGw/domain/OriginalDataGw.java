package com.vaiv.analyticsManager.apiGw.domain;

import lombok.Data;

@Data
public class OriginalDataGw {
	private Integer originalDataId;
	private String name;
	private String filepath;
	private String filename;
	private String extension;
	private String createDatetime;
	private boolean deleteFlag;
	private Integer projectId;
	private Integer instanceId;
	private String columns;
	private String statistics;
	private String sampleData;
	private Integer amount;

}
