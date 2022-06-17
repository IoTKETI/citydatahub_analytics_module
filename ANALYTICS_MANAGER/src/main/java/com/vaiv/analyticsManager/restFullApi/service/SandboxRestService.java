package com.vaiv.analyticsManager.restFullApi.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.mapper.SandboxRestMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class SandboxRestService {
	
	@Autowired
	private SandboxRestMapper sandboxRestMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Value("${cloudApi.url}")
	private String cloudApiUrl;

	@Value("${allModel.modelListUrl}")
	private String adminModelListUrl;

	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	@Value("${module.localFiles}")
	private String moduleLocalFiles;

	/**
	 * 인스턴스 리스트 조회
	 * @return
	 * @throws Exception
	 */
	public JSONObject instances(HttpSession session) throws Exception{
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = sandboxRestMapper.instances();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ){
				jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("instances", jsonArr);
		return resultJson;
	}

	/**
	 * 전체 모델 목록 가져오기
	 * @return
	 */
	public JSONObject allModelList() throws Exception {

		JSONObject resultJson = new JSONObject();
		resultJson.put("result", "success");
		resultJson.put("type", "2000");

		// 1. Get All Modle List from Keti Core Module
		String url = adminModelListUrl;

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).addHeader("accept", "application/ld+json").build();

		String resMessage = "";
		Response response = client.newCall(request).execute();
		resMessage = response.body().string();

		// 2. Transform to a Same format like availablelist
		JSONArray allModleList = new JSONArray();
		JSONArray modelList = JSONArray.fromObject(resMessage);
		for(Object jsonObject : modelList){
			JSONObject modelInfo=new JSONObject();
			modelInfo.put("id", ((JSONObject)jsonObject).get("typeUri").toString());
			modelInfo.put("name", ((JSONObject)jsonObject).get("type").toString());
			
			allModleList.add(modelInfo);
		}

		resultJson.put("availableList", JSONArray.fromObject(allModleList.toString()));

		return resultJson;
	}
	
	/**
	 * 로컬파일 조회
	 * @param selectedInstance
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFiles(Integer selectedInstance) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray fileListJson = null;
		
		// 인스턴스가 있는지 확인
		Map<String, Object> detail = sandboxRestMapper.instance(selectedInstance);
		if( detail == null ) {
			resultJson.put("type", "http://citydatahub.kr/errors/ResourceNotFound");
			resultJson.put("title", "Resource Not Found");
			resultJson.put("detail", "Not found instance");
			return resultJson;
		}
		
		// 인스턴스 내부IP 가져오기
		String ip = getInstanceIp(selectedInstance);
		String listUrl = ip + moduleLocalFiles;
		resultJson = httpService.httpServiceGET(listUrl, "");
		resultJson = JSONObject.fromObject(resultJson.get("data"));
		if( MakeUtil.isNotNullAndEmpty(resultJson.get("result")) )
			resultJson = JSONObject.fromObject(resultJson.get("result"));
		if( MakeUtil.isNotNullAndEmpty(resultJson.get("file_list")) )
			fileListJson = JSONArray.fromObject(resultJson.get("file_list"));
		
		resultJson = new JSONObject();
		resultJson.put("localFiles", fileListJson);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
		
	}

	/**
	 * 로컬파일 샘플 조회
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFileSample(Integer selectedInstance, String localFile) throws Exception {
		// 인스턴스 내부IP 가져오기
		String ip = getInstanceIp(selectedInstance);
		String listUrl = ip + "/localFiles?path=/"+localFile+"&&command=get_sample";
		
		JSONObject resultJson = new JSONObject();
		JSONObject localFileJson = httpService.httpServiceGET(listUrl, "");
		
		resultJson.put("localFile", localFileJson.get("data"));
		resultJson.put("result", "success");
		resultJson.put("type", "2000");

		return resultJson;
	}


	public String getPrivateIpaddressWithUserIdAndInstancetId(String userId, Integer instanceIdNum) {
		return sandboxRestMapper.getPrivateIpaddressWithUserIdAndInstancetId(userId, instanceIdNum);
	}

	public String getPrivateIpaddressWithInstanceId(Integer instanceIdNum) {
		return sandboxRestMapper.getPrivateIpaddressWithInstanceId(instanceIdNum);
	}
	
	/**
	 * 내부IP 가져오기
	 * @param instancePk
	 * @return
	 * @throws Exception
	 */
	public String getInstanceIp(Integer instanceSequencePk) throws Exception {
		Map<String, Object> instance = sandboxRestMapper.instance(instanceSequencePk);
		String ip = "http://" + instance.get("PRIVATE_IP") + ":" + modulePort + moduleMethod;
		
		if( MakeUtil.isNotNullAndEmpty(moduleTempUrl) )	ip = moduleTempUrl + ":" + modulePort + moduleMethod;
		
		return ip;
	}
}
