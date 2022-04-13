package com.vaiv.analyticsManager.apiGw.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.apiGw.domain.ModelGw;
import com.vaiv.analyticsManager.apiGw.domain.OriginalDataGw;
import com.vaiv.analyticsManager.apiGw.domain.PreprocessedDataGw;
import com.vaiv.analyticsManager.apiGw.domain.ProjectGw;

@Mapper
public interface ProjectGwMapper {

	List<Map<String, Object>> projectsGw(String userId);

	Map<String, Object> projectGw(Integer projectSequencePk);
	
	void insertProjectGw(ProjectGw projectGw);
	
	void updateProjectGw(ProjectGw projectGw);
	

	List<Map<String, Object>> originalDataListGw(Integer projectSequencePk);

	Map<String, Object> originalDataGw(Integer projectSequencePk, Integer originalDataSequencePk);
	
	void insertOriginalDataGw(OriginalDataGw originalDataGw);
	
	
	

	List<Map<String, Object>> preprocessFunctionListGw();

	Map<String, Object> preprocessFunctionGw(Integer preprocessFunctionSequencePk);
	
	
	
	
	List<Map<String, Object>> preprocessedDataListGw(Integer instanceId, Integer originalDataId);

	Map<String, Object> preprocessedDataGw(Integer instanceId, Integer preprocessedDataId);
	
	
	

	List<Map<String, Object>> modelsListGw(ModelGw modelGw);

	Map<String, Object> modelGw(Integer projectId, Integer modelId);

	List<Map<String, Object>> projectsByinstanceIdGw(int id);

	int checkProjectNameGw(ProjectGw projectGw);

	int checkDuplicateOriginalDataGw(OriginalDataGw originalDataGw);

	void deleteOriginalDataGw(OriginalDataGw originalDataGw);

	void insertPreprocessedDataGw(PreprocessedDataGw pData);

	void updatePreprocessedDataGw(PreprocessedDataGw pData);

	void insertModelGw(ModelGw modelGw);

	void updateModelsGw(ModelGw modelGw);


}
