package com.vaiv.analyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.vaiv.analyticsManager.restFullApi.domain.Instance;
import com.vaiv.analyticsManager.restFullApi.domain.Template;

@Mapper
public interface SandboxRestMapper {

	List<Map<String, Object>> instances(String userId) throws Exception;

	Map<String, Object> instance(Integer instancePk) throws Exception;

	List<Map<String, Object>> templates(String userId) throws Exception;

	Map<String, Object> template(Integer templateId) throws Exception;

	List<Map<String, Object>> customTemplateRequests(String userId) throws Exception;

	Map<String, Object> customTemplateRequest(Integer templateId) throws Exception;

	void customTemplateRequestsAsPost(Template template) throws Exception;

	void customTemplateRequestsAsPatch(Template template) throws Exception;

	void templatesAsPost(Template template) throws Exception;

	int checkTemplateName(String name) throws Exception;

	void templateUser(Template template) throws Exception;

	void templateAsDelete(Integer templateId) throws Exception;

	void deleteTemplateUser(Integer templateId) throws Exception;

	void templatesAsPatch(Template template) throws Exception;

	void insertInstance(Instance instance) throws Exception;

	void insertInstanceDetail(Instance instance) throws Exception;

	List<Map<String, Object>> InstancesOfServerState(String serverState) throws Exception;

	void updateInstance(Instance instance) throws Exception;

	int checkInstanceName(String name) throws Exception;

	List<Map<String, Object>> templateUsers(Integer templateId) throws Exception;

	String getPrivateIpaddressWithUserIdAndInstancetId(String userId, Integer instanceIdNum);

	String getPrivateIpaddressWithInstanceId(Integer instanceIdNum);

	

}
