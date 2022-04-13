package com.vaiv.analyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlgorithmRestMapper {

	List<Map<String, Object>> algorithms() throws Exception;

	Map<String, Object> algorithm(Integer id) throws Exception;

	List<Map<String, Object>> searchAlgorithms(String searchValue) throws Exception;


}