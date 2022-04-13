package com.vaiv.analyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.restFullApi.domain.Model;
import com.vaiv.analyticsManager.restFullApi.domain.OriginalData;
import com.vaiv.analyticsManager.restFullApi.domain.PreprocessedData;
import com.vaiv.analyticsManager.restFullApi.domain.Project;

@Mapper
public interface ProjectRestMapper {

	List<Map<String, Object>> projects(String userId) throws Exception;;

	Map<String, Object> project(Integer projectSequencePk) throws Exception;;

	int checkProjectName(Project project) throws Exception;;

	void insertProject(Project project) throws Exception;;

	void updateProject(Project project) throws Exception;;

	List<Map<String, Object>> originalDataList(Integer projectSequencePk) throws Exception;

	Map<String, Object> originalData(Integer projectSequencePk, Integer originalDataSequencePk) throws Exception;

	void insertOriginalData(OriginalData origianlData) throws Exception;

	void deleteOriginalData(OriginalData origianlData) throws Exception;

	List<Map<String, Object>> preprocessFunctionList() throws Exception;

	Map<String, Object> preprocessFunction(Integer preprocessFunctionSequencePk) throws Exception;

	int checkDuplicateOriginalData(OriginalData originalData) throws Exception;

	List<Map<String, Object>> projectsByinstancePk(int instancePk) throws Exception;

	void insertPreprocessedData(PreprocessedData preprocessedData) throws Exception;
			   
	List<Map<String, Object>> preprocessedDataList(Integer instancePk, Integer originalDataSequencePk) throws Exception;

	Map<String, Object> preprocessedData(Integer instancePk, Integer preprocessedDataSequencePk) throws Exception;

	void updatePreprocessedData(PreprocessedData pData) throws Exception;

	void insertModel(Model model) throws Exception;

	void updateModels(Model model) throws Exception;

	List<Map<String, Object>> modelsList(Model model) throws Exception;

	Map<String, Object> model(Integer projectSequencePk, Integer modelSequencePk) throws Exception;

	List<Map<String, Object>> modelsOfInstancePk(Integer instanceSequencePk);




}
