package com.vaiv.analyticsManager.apiGw.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlgorithmGwMapper {

	List<Map<String, Object>> algorithmsGw() throws Exception;

	Map<String, Object> algorithmGw(Integer id) throws Exception;

}