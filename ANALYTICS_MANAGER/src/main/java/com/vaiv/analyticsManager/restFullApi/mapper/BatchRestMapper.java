package com.vaiv.analyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.restFullApi.domain.Batch;
import com.vaiv.analyticsManager.restFullApi.domain.SearchData;

@Mapper
public interface BatchRestMapper {

	List<Map<String, Object>> batchServiceRequests(String userId) throws Exception;

	Map<String, Object> batchServiceRequest(Integer batchServiceSequencePk) throws Exception;
	
	void insertBatchServiceRequest(Batch batch) throws Exception;

	void updateBatchServiceRequest(Batch batch) throws Exception;
	
	List<Map<String, Object>> batchServices(String userId) throws Exception;

	Map<String, Object> batchService(Integer batchServiceSequencePk) throws Exception;

	int checkBatchName(Batch batch) throws Exception;

	void insertBatchServices(Batch batch) throws Exception;

	void updateBatchService(Batch batch) throws Exception;

	List<Map<String, Object>> batchServers() throws Exception;

	List<Map<String, Object>> batchServiceByinstancePk(int instancePk) throws Exception;
	
	List<Map<String, Object>> batchServiceRequestsByinstancePk(int instancePk) throws Exception;

	void updateBatchServiceUseFlag(Batch batch);

	List<Map<String, Object>> batchLogs(SearchData searchData);

	Map<String, Object> batchLog(Integer logBatchSequencePk);

	int batchLogsSearchTotalCount(SearchData searchData);

	int batchLogsTotalCount(SearchData searchData);
	

}
