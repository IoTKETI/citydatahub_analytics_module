package com.vaiv.analyticsManager.apiGw.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.apiGw.mapper.SandboxGwMapper;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class SandboxGwService {
	
	@Autowired
	private SandboxGwMapper sandboxGwMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private RestFullReturnService restFullReturnService;


	// Cloud API
	@Value("${cloudApi.url}")
	private String cloudApiUrl;

	//module Api
	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	@Value("${module.localFiles}")
	private String moduleLocalFiles;

	/**
	 * 인스턴스 리스트 조회 API
	 * @return
	 */
	public JSONArray instancesGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxGwMapper.instancesGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				JSONObject j = JSONObject.fromObject(map.get("entities"));
				if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) map.put("entities", j.get("availableList"));
				
				jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
			}
		}
		
		return jsonArr;
	}

	/**
	 * 인스턴스 개별 조회 API
	 * @param instancePk
	 * @return
	 */
	public JSONObject instanceGw(String instancePk) throws Exception {
		JSONObject result = new JSONObject();
		int id;
		try {
			id = Integer.parseInt(instancePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = sandboxGwMapper.instanceGw(id);
		
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			JSONObject j = JSONObject.fromObject(detail.get("entities"));
			if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) detail.put("entities", j.get("availableList"));
			
		}else {
			return restFullReturnService.resourceNotFound("Not found instance");
		}
		
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}

	/**
	 * 인스턴스 로컬파일 조회 API
	 * @param selectedInstance
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFilesGw(Integer selectedInstance) throws Exception {
		JSONObject httpResult = new JSONObject();
		JSONObject result = new JSONObject();
		JSONArray fileListJson = null;

		// 인스턴스 내부IP 가져오기
		String ip = getInstanceIp(selectedInstance);
		String listUrl = ip + moduleLocalFiles;
		httpResult = httpService.httpServiceGET(listUrl, "");
		//{"type":"200","title":"OK","data":{"command":"get_list","pathhttpResultult":{}}}
		httpResult = JSONObject.fromObject(httpResult.get("data"));
		if( MakeUtil.isNotNullAndEmpty(httpResult.get("result")) )
			httpResult = JSONObject.fromObject(httpResult.get("result"));
		if( MakeUtil.isNotNullAndEmpty(httpResult.get("file_list")) )
			fileListJson = JSONArray.fromObject(httpResult.get("file_list"));
		
		result.put("localFiles", fileListJson);
		return result;
	}

	/**
	 * 인스턴스 로컬파일 샘플 조회 API
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFileSampleGw(Integer selectedInstance, String localFile) throws Exception {
		JSONObject httpResult = new JSONObject();
		JSONObject result = new JSONObject();
		
		// 인스턴스 내부IP 가져오기
		String ip = getInstanceIp(selectedInstance);
		String listUrl = ip + "/localFiles?path=/"+localFile+"&&command=get_sample";
		
		httpResult = httpService.httpServiceGET(listUrl, "");
		
		result.put("localFile", httpResult.get("data"));
		return result;
	}
	
	
	/**
	 * 인스턴스 내부IP 가져오기
	 * @param instancePk
	 * @return
	 * @throws Exception
	 */
	public String getInstanceIp(Integer instanceSequencePk) throws Exception {
		Map<String, Object> instance = sandboxGwMapper.instanceGw(instanceSequencePk);
		String ip = "http://" + instance.get("PRIVATE_IP") + ":" + modulePort + moduleMethod;
		
		if( MakeUtil.isNotNullAndEmpty(moduleTempUrl) )	ip = moduleTempUrl + ":" + modulePort + moduleMethod;
		
		return ip;
	}

}
