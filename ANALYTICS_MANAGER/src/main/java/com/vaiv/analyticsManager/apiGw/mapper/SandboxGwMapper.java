package com.vaiv.analyticsManager.apiGw.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SandboxGwMapper {

	List<Map<String, Object>> instancesGw(String userId);

	Map<String, Object> instanceGw(Integer instancePk);

}
