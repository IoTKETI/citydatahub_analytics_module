package com.vaiv.restFull.mapper;

import java.util.List;
import java.util.Map;

import com.vaiv.restFull.domain.BatchServiceState;
import com.vaiv.restFull.domain.LogBatch;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BatchMapper {

	List<Map<String, Object>> batchServicesAll();
	List<Map<String, Object>> batchServices(Long sandboxInstanceSequence);

	Map<String, Object> batchService(Long batchServiceSequencePk);

	Map<String, Object> instance(Long instanceSequencePk);

	void insertLogBatch(LogBatch logBatch) throws Exception;

	void updateBatchServiceState(BatchServiceState batchServiceState);
}
