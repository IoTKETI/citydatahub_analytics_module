package com.vaiv.analyticsManager.restFullApi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import okhttp3.Headers;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Instance;
import com.vaiv.analyticsManager.restFullApi.domain.Template;
import com.vaiv.analyticsManager.restFullApi.mapper.BatchRestMapper;
import com.vaiv.analyticsManager.restFullApi.mapper.ProjectRestMapper;
import com.vaiv.analyticsManager.restFullApi.mapper.SandboxRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@SuppressWarnings("static-access")
public class SandboxRestService {
	
	private static Logger logger = LoggerFactory.getLogger(SandboxRestService.class);
	
	@Autowired
	private SandboxRestMapper sandboxRestMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private ProjectRestService projectRestService;
	
	@Autowired
	private ProjectRestMapper projectRestMapper;
	
	@Autowired
	private BatchRestService batchRestService;
	
	@Autowired
	private BatchRestMapper batchRestMapper;

	// Cloud API
	@Value("${cloudApi.url}")
	private String cloudApiUrl;

	@Value("${cloudApi.credential}")
	private String cloudApiCredential;

	@Value("${cloudApi.authorization}")
	private String cloudApiAuthorization;

	@Value("${cloudApi.privateIpRange}")
	private String privateIpRange;

	@Value("${cloudApi.publicIpRange}")
	private String publicIpRange;


	@Value("${cloudApi.sandbox.keyName}")
	private String keyName;

	@Value("${cloudApi.sandbox.availabilityZone}")
	private String availabilityZone;

	@Value("${cloudApi.sandbox.networks}")
	private String networks;

	@Value("${cloudApi.sandbox.securityGroups}")
	private String securityGroups;


	ArrayList<SubnetUtils> publicSubnetUtils=null;
	ArrayList<SubnetUtils> privateSubnetUtils=null;

	private final String CLOUD_API_FLAVOR_POST_FIX="/openstack/infra/cloudServices/openstack/flavors";
	private final String CLOUD_API_SERVER_POST_FIX="/openstack/infra/cloudServices/openstack/servers";
	private final String CLOUD_API_ALL_IMAGE_POST_FIX="/openstack/infra/cloudServices/openstack/images";

	private final String CLOUD_API_CREDENTIAL_KEY="credential";
	private final String CLOUD_API_AUTHORIZATION_KEY="Authorization";
	private final String CLOUD_API_SERVER_START_COMMAND="start";
	private final String CLOUD_API_SERVER_STOP_COMMAND="stop";


	@Value("${allModel.modelListUrl}")
	private String adminModelListUrl;

	// @Value("${allModel.devTest}")
	// private Boolean adminDevTest;

	// @Value("${allModel.devSampleModels}")
	// private String devSampleModels;

	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	@Value("${module.localFiles}")
	private String moduleLocalFiles;

	@PostConstruct
	public void initSandboxRestServcie(){

		this.publicSubnetUtils=new ArrayList<SubnetUtils> ();
		this.privateSubnetUtils=new ArrayList<SubnetUtils> ();

		String[] privateIpList= this.privateIpRange.split(",");
		String[] publicIpList= this.publicIpRange.split(",");

		for(int i=0;i<privateIpList.length;i++){
			this.privateSubnetUtils.add(new SubnetUtils(privateIpList[i]));
		}

		for(int j=0;j<publicIpList.length;j++){
			this.publicSubnetUtils.add(new SubnetUtils(publicIpList[j]));
		}

		for(SubnetUtils publicSubnetUtil :publicSubnetUtils){
			logger.info("샌드박스 서비스의 등록된 공인 아이피 대역 : "+publicSubnetUtil.getInfo().getNetworkAddress());

		}

		for(SubnetUtils privateSubnetUtil :privateSubnetUtils){
			logger.info("샌드박스 서비스의 등록된 사설 아이피 대역 : "+privateSubnetUtil.getInfo().getNetworkAddress());
		}

	}

	/**
	 * 샌드박스 리스트 조회
	 * @return
	 * @throws Exception
	 */
	public JSONObject instances(HttpSession session) throws Exception{
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxRestMapper.instances(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("instances", jsonArr);
		return resultJson;
	}

	/**
	 * 샌드박스 개별 조회
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject instance(Integer instancePk) throws Exception{
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = sandboxRestMapper.instance(instancePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("instance", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		logger.info(resultJson.getString("instance").toString());
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 샌드박스 사양 조회
	 * @return
	 * @throws Exception
	 */
	public JSONObject specifications() throws Exception{

		JSONObject returnJson = null;

		String url = cloudApiUrl + CLOUD_API_FLAVOR_POST_FIX;

		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		logger.info(url);
		logger.info(headers.toString());

		returnJson = httpService.httpServiceGET(url, headers);
		returnJson.put("result", "success");
		returnJson.put("type", "2000");

		logger.info(returnJson.toString());

		return returnJson;
	}

	
	/**
	 * 샌드박스 사양 상세조회
	 * @param flavorId
	 * @return
	 * @throws Exception
	 */
	public JSONObject specification(String flavorId) throws Exception {


		JSONObject httpResponseJson = null;
		JSONObject flavorJson = null;
		JSONObject returnJson = new JSONObject();
		JSONArray flavorArrayJson= null;
		String url = cloudApiUrl+CLOUD_API_FLAVOR_POST_FIX+"/"+flavorId;

		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		httpResponseJson = httpService.httpServiceGET(url, headers);

		flavorArrayJson = new JSONArray().fromObject(httpResponseJson.get("data"));

		if( httpResponseJson.get("type").equals("200") ) {
			flavorJson=(JSONObject) flavorArrayJson.get(0);
			returnJson.put("result", "success");
			returnJson.put("type", "2000");
			returnJson.put("name", flavorJson.get("name"));
			returnJson.put("vcpus", flavorJson.get("vcpus"));
			returnJson.put("ram", flavorJson.get("ram"));
			returnJson.put("eDisk", flavorJson.get("ephemeral"));
			returnJson.put("disk", flavorJson.get("disk"));

			return returnJson;
		}else {
			returnJson.put("type", "5000");
			returnJson.put("detail", httpResponseJson.toString());

			return returnJson;
		}
	}

	/**
	 * 샌드박스 템플릿 조회
	 * @return
	 */
	public JSONObject templates(HttpSession session) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxRestMapper.templates(userId);
		
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				jsonArr.add(new JSONObject().fromObject(map));
			}
		}
		resultJson.put("templates", jsonArr);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");

		return resultJson;
	}

	/**
	 * 샌드박스 템플릿 상세조회
	 * @param templateId
	 * @return
	 */
	public JSONObject template(Integer templateId) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Map<String, Object> detail = sandboxRestMapper.template(templateId);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("template", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		
		resultJson.put("userList", jsonArr);
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 샌드박스 템플릿 신청 이력 조회
	 * @return
	 */
	public JSONObject customTemplateRequests(HttpSession session) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxRestMapper.customTemplateRequests(userId);
		
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}
		resultJson.put("customTemplateRequests", jsonArr);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 샌드박스 템플릿 신청 이력 개별조회
	 * @param templateId
	 * @return
	 */
	public JSONObject customTemplateRequest(Integer templateId) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = sandboxRestMapper.customTemplateRequest(templateId);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("customTemplateRequest", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


	/**
	 * 템플릿 허용 목록 가져오기
	 * @return
	 */
	// public JSONObject availableList(String userId) throws Exception {

	// 	if(devTest==true)
	// 	{
	// 		userId=devUserId;
	// 	}

	// 	String url = modelListUrl + urlPrefix + "/" + userId + urlPostfix;

	// 	OkHttpClient client = new OkHttpClient();
	// 	Request request = new Request.Builder().url(url).build();
	// 	String resMessage = "";

	// 	/* 서버 불안정으로 잠시 처리 */
	// 	Response response = client.newCall(request).execute(); 
	// 	resMessage=response.body().string();
	// 	logger.info(resMessage);
	// 	JSONObject resultJson = new JSONObject();
	// 	resultJson.put("result", "success");
	// 	resultJson.put("type", "2000");
	// 	resultJson.put("availableList", new JSONArray().fromObject(resMessage));

	// 	return resultJson;
	// }

	/**
	 * 템플릿 전체 모델 목록 가져오기
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
		JSONArray modelList = new JSONArray().fromObject(resMessage);
		for(Object jsonObject : modelList){
			JSONObject modelInfo=new JSONObject();
			modelInfo.put("id", ((JSONObject)jsonObject).get("typeUri").toString());
			modelInfo.put("name", ((JSONObject)jsonObject).get("type").toString());
			
			allModleList.add(modelInfo);
		}

		resultJson.put("availableList", new JSONArray().fromObject(allModleList.toString()));

		return resultJson;
	}
	
	// /**
	//  * 스냅샷 목록 가져오기
	//  * @return
	//  */
	// public JSONObject snapshotList() throws Exception {

	// 	JSONObject httpResponseJson = null;
	// 	JSONObject imageJson = null;
	// 	JSONObject selectedSnapshotJson = null;
	// 	JSONObject returnJson = new JSONObject();
	// 	JSONArray imageArrayJson= null;
	// 	JSONArray snapshotArrayJson =  new JSONArray();
	// 	String url = cloudApiUrl+CLOUD_API_ALL_IMAGE_POST_FIX;

	// 	Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
	// 											.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
	// 											.add("Content-Type", "application/json").build();
	// 	logger.info("############## 스냅샷 목록 가져오기 ################");
	// 	logger.info(url);
	// 	logger.info(headers.toString());

	// 	httpResponseJson = httpService.httpServiceGET(url, headers);
	// 	imageArrayJson = new JSONArray().fromObject(httpResponseJson.get("data"));

	// 	logger.info(httpResponseJson.toString());

	// 	if( httpResponseJson.get("type").equals("200") ) {

	// 		for (int i = 0; i < imageArrayJson.size(); i++) {
	// 			imageJson = new JSONObject().fromObject(imageArrayJson.get(i));

	// 			//if( imageJson.get("type").equals("snapshot")
	// 			if( imageJson.get("type").equals("image")
	// 					&& (""+imageJson.get("name")).contains("sandbox") ){
	// 				selectedSnapshotJson = new JSONObject();
	// 				selectedSnapshotJson.put("name", imageJson.get("name"));
	// 				selectedSnapshotJson.put("id", imageJson.get("id"));
	// 				snapshotArrayJson.add(selectedSnapshotJson);
	// 			}

	// 		returnJson.put("result", "success");
	// 		returnJson.put("type", "2000");
	// 		returnJson.put("snapshotList", snapshotArrayJson);

	// 		return returnJson;
	// 	}else {
	// 		returnJson.put("type", "5000");
	// 		returnJson.put("detail", httpResponseJson.toString());

	// 		return returnJson;
	// 	}
	// }

	
	/**
	 * 템플릿 허용 데이터 가져오기
	 * @param id
	 * @return
	 */
	public JSONObject availableDataList(String id) throws Exception {
		// 임시 데이터 생성
		JSONObject resultJson = new JSONObject();
		String availableDataList = "";
		if( id.equals("park") ) {
			availableDataList = "{\"data\":[{\"id\":\"dongtan\",\"name\":\"동탄\"},{\"id\":\"bundang\",\"name\":\"분당동\"},{\"id\":\"sunae\",\"name\":\"수내동\"}]}";
		}else if( id.equals("air") ) {
			availableDataList = "{\"data\":[{\"id\":\"jeongja\",\"name\":\"정자\"},{\"id\":\"gakseongdae\",\"name\":\"낙성대\"},{\"id\":\"kimchon\",\"name\":\"김천\"}]}";
		}else { //weather
			availableDataList = "{\"data\":[{\"id\":\"gangnam\",\"name\":\"강남\"},{\"id\":\"hannam\",\"name\":\"한남\"},{\"id\":\"busan\",\"name\":\"부산\"}]}";
		}
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("availableDataList", new JSONObject().fromObject(availableDataList));
		return resultJson;
	}
	
	/**
	 * 샌드박스 로컬파일 조회
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
		
		// 실행중인지 확인
		if( "start_done".equals(detail.get("SERVER_STATE")) ) {
			// 인스턴스 내부IP 가져오기
			String ip = getInstanceIp(selectedInstance);
			String listUrl = ip + moduleLocalFiles;
			resultJson = httpService.httpServiceGET(listUrl, "");
			//{"type":"200","title":"OK","data":{"command":"get_list","path":"/","result":{}}}
			resultJson = new JSONObject().fromObject(resultJson.get("data"));
			if( MakeUtil.isNotNullAndEmpty(resultJson.get("result")) )
				resultJson = new JSONObject().fromObject(resultJson.get("result"));
			if( MakeUtil.isNotNullAndEmpty(resultJson.get("file_list")) )
				fileListJson = new JSONArray().fromObject(resultJson.get("file_list"));
			
			resultJson = new JSONObject();
			resultJson.put("localFiles", fileListJson);
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
			return resultJson;
			
		}else {
			resultJson.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
			resultJson.put("title", "Operation Not Supported");
			resultJson.put("detail", "Instance Stop Status");
			return resultJson;
		}
		
	}

	/**
	 * 샌드박스 로컬파일 샘플 조회
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFileSample(Integer selectedInstance, String localFile) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject localFileJson = null;
		
		// 인스턴스 내부IP 가져오기
		String ip = getInstanceIp(selectedInstance);
		String listUrl = ip + "/localFiles?path=/"+localFile+"&&command=get_sample";
		
		localFileJson = httpService.httpServiceGET(listUrl, "");
		
		resultJson = new JSONObject();
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
	 * 샌드박스 내부IP 가져오기
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
