package com.vaiv.analyticsManager.apiGw.domain;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SearchDataGw {
	private Integer iDisplayStart;
	private Integer iDisplayLength;
	private Integer iColumns;
	private String sSearch;
	private boolean bRegex;
	private boolean bSearchable_0;
	private String sSearch_0;
	private boolean bRegex_0;
	private boolean bSortable_0;
	private Integer iSortingCols;
	private Integer iSortCol_0;
	private String sSortDir_0;
	private Integer iTotalRecords;
	private Integer iTotalDisplayRecords;
	private String sEcho;
	
	private String startDate;
	private String endDate;
	private String userId;
	private String[] columns;
	private String sSortCol;
	
	private List<Map<String, Object>> aaData;
	
}
