package com.vaiv.analyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SandboxRestMapper {

	List<Map<String, Object>> instances() throws Exception;

	Map<String, Object> instance(Integer instancePk) throws Exception;

	String getPrivateIpaddressWithUserIdAndInstancetId(String userId, Integer instanceIdNum);

	String getPrivateIpaddressWithInstanceId(Integer instanceIdNum);

	

}
