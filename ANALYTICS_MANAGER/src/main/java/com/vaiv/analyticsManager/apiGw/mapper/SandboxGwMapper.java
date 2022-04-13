package com.vaiv.analyticsManager.apiGw.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.apiGw.domain.InstanceGw;
import com.vaiv.analyticsManager.apiGw.domain.TemplateGw;

@Mapper
public interface SandboxGwMapper {

	
	List<Map<String, Object>> customAnalysisTemplateRequestsGw(String userId);

	Map<String, Object> customAnalysisTemplateRequestGw(Integer customTemplateId);
	
	void customAnalysisTemplateRequestsAsPostGw(TemplateGw templateGw);

	void customAnalysisTemplateRequestsAsPatchGw(TemplateGw templateGw);
	
	
	
	List<Map<String, Object>> analysisTemplatesGw(String userId);

	Map<String, Object> analysisTemplateGw(Integer templateId);
	
	void analysisTemplatesAsPostGw(TemplateGw templateGw);
	
	void analysisTemplatesAsPatchGw(TemplateGw templateGw);
	
	void analysisTemplatesAsDeleteGw(Integer templateId);
	

	

	List<Map<String, Object>> instancesGw(String userId);

	Map<String, Object> instanceGw(Integer instancePk);

	int checkTemplateNameGw(String name);

	void customTemplateRequestsAsPatchGw(TemplateGw templateGw);

	void templateUserGw(TemplateGw templateGw);

	void deleteTemplateUserGw(Integer templateId);

	int checkInstanceNameGw(String name);

	void insertInstanceGw(InstanceGw instanceGw);

	void insertInstanceDetailGw(InstanceGw instanceGw);

	void updateInstanceGw(InstanceGw instanceGw);

}
