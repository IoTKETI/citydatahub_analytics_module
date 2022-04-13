package com.vaiv.analyticsManager.apiGw.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.apiGw.domain.BatchGw;

@Mapper
public interface BatchGwMapper {

	List<Map<String, Object>> batchServiceRequestsGw(String userId);

	Map<String, Object> batchServiceRequestGw(Integer batchServiceRequestId, String userId);

	void insertBatchServiceRequestGw(BatchGw batchGw);
	
	void updateBatchServiceRequestGw(BatchGw batchGw);
	
	
	
	List<Map<String, Object>> batchServicesGw(String userId);

	Map<String, Object> batchServiceGw(Integer batchServiceId, String userId);

	void insertBatchServicesGw(BatchGw batchGw);

	int checkBatchNameGw(BatchGw batchGw);

	void updateBatchServiceGw(BatchGw batchGw);

	List<Map<String, Object>> batchServiceRequestsByinstanceIdGw(int id);

	List<Map<String, Object>> batchServiceByinstanceIdGw(int id);

	
	
	List<Map<String, Object>> batchServersGw();

	Map<String, Object> batchServerGw(int batchInstanceId);

	void updateBatchServiceUseFlagGw(BatchGw batchGw);

	List<Map<String, Object>> batchLogsGw(String userId, String startDate, String endDate);

	Map<String, Object> batchLogGw(Integer logBatchId, String userId);


}
