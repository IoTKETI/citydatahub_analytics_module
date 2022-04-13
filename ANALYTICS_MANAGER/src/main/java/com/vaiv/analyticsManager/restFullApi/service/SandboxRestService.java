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


	@Value("${authorizedModel.modelListUrl}")
	private String modelListUrl;
	
	@Value("${authorizedModel.urlPrefix}")
	private String urlPrefix;
	
	@Value("${authorizedModel.urlPostfix}")
	private String urlPostfix;

	@Value("${authorizedModel.devTest}")
	private Boolean devTest;

	@Value("${authorizedModel.devID}")
	private String devUserId;


	@Value("${allModel.modelListUrl}")
	private String adminModelListUrl;

	@Value("${allModel.devTest}")
	private Boolean adminDevTest;

	@Value("${allModel.devSampleModels}")
	private String devSampleModels;

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
		// added 2022.01.26 by RJH
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

		// Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
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
	public JSONObject availableList(String userId) throws Exception {

		if(devTest==true)
		{
			userId=devUserId;
		}

		String url = modelListUrl + urlPrefix + "/" + userId + urlPostfix;

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).build();
		String resMessage = "";

		/* 서버 불안정으로 잠시 처리 */
		Response response = client.newCall(request).execute(); 
		resMessage=response.body().string();
		logger.info(resMessage);
		JSONObject resultJson = new JSONObject();
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("availableList", new JSONArray().fromObject(resMessage));

		return resultJson;
	}

	/**
	 * 템플릿 전체 모델 목록 가져오기
	 * @return
	 */
	public JSONObject allModelList() throws Exception {

		JSONObject resultJson = new JSONObject();
		resultJson.put("result", "success");
		resultJson.put("type", "2000");

		// 1. If this is the devMode use the sample data at application yml
		if(adminDevTest==true)
		{
			resultJson.put("availableList", devSampleModels);
			return resultJson;
		}


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
	
	/**
	 * 스냅샷 목록 가져오기
	 * @return
	 */
	public JSONObject snapshotList() throws Exception {

		JSONObject httpResponseJson = null;
		JSONObject imageJson = null;
		JSONObject selectedSnapshotJson = null;
		JSONObject returnJson = new JSONObject();
		JSONArray imageArrayJson= null;
		JSONArray snapshotArrayJson =  new JSONArray();
		String url = cloudApiUrl+CLOUD_API_ALL_IMAGE_POST_FIX;

		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();
		logger.info("############## 스냅샷 목록 가져오기 ################");
		logger.info(url);
		logger.info(headers.toString());

		httpResponseJson = httpService.httpServiceGET(url, headers);
		imageArrayJson = new JSONArray().fromObject(httpResponseJson.get("data"));

		logger.info(httpResponseJson.toString());

		if( httpResponseJson.get("type").equals("200") ) {

			for (int i = 0; i < imageArrayJson.size(); i++) {
				imageJson = new JSONObject().fromObject(imageArrayJson.get(i));

				//if( imageJson.get("type").equals("snapshot")
				if( imageJson.get("type").equals("image")
						&& (""+imageJson.get("name")).contains("sandbox") ){
					selectedSnapshotJson = new JSONObject();
					selectedSnapshotJson.put("name", imageJson.get("name"));
					selectedSnapshotJson.put("id", imageJson.get("id"));
					snapshotArrayJson.add(selectedSnapshotJson);
				}

				/*if( imageJson.get("snapshot").equals("true")
						&& (""+imageJson.get("name")).contains("sandbox") ){
					selectedSnapshotJson = new JSONObject();
					selectedSnapshotJson.put("name", imageJson.get("name"));
					selectedSnapshotJson.put("id", imageJson.get("id"));
					snapshotArrayJson.add(selectedSnapshotJson);
				}*/
			}

			returnJson.put("result", "success");
			returnJson.put("type", "2000");
			returnJson.put("snapshotList", snapshotArrayJson);

			return returnJson;
		}else {
			returnJson.put("type", "5000");
			returnJson.put("detail", httpResponseJson.toString());

			return returnJson;
		}
	}

	
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
	 * 샌드박스 템플릿 추가 요청
	 * @param template
	 * @throws Exception
	 */
	public void customTemplateRequestsAsPost(Template template) throws Exception {
		template.setDataSummaryToString(template.getDataSummary().toString());
		sandboxRestMapper.customTemplateRequestsAsPost(template);
	}
	
	/**
	 * 샌드박스 템플릿  생성요청 취소 또는 커스텀 샌드박스 관리자 승인 또는 거절 또는 완료
	 * @param templateId
	 * @return
	 */
	public void customTemplateRequestsAsPatch(Template template) throws Exception {
		sandboxRestMapper.customTemplateRequestsAsPatch(template);
	}

	/**
	 * 템플릿 생성
	 * @param template
	 */
	public JSONObject templatesAsPost(Template template) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		// 템플릿 명 중복 체크
		if( sandboxRestMapper.checkTemplateName(template.getName()) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			template.setDataSummaryToString(template.getDataSummary().toString());
			
			// 사용자의 커스텀 템플릿 진행상태 변경
			if( MakeUtil.isNotNullAndEmpty(template.getCustomTemplateId()) ) {
				template.setTemplateId(template.getCustomTemplateId());
				template.setProgressState("done");
				template.setAdminComment("생성완료");
				sandboxRestMapper.customTemplateRequestsAsPatch(template);
			}

			// 템플릿 생성
			sandboxRestMapper.templatesAsPost(template);
			
			// 템플릿 사용자 넣어주기
			if( !template.isPublicFlag() ) {
				template.setTemplateId(template.getANALYSIS_TEMPLATE_SEQUENCE_PK());
				List<String> userList = Arrays.asList(template.getUserIdList().split(","));
				for( String userId : userList ) {
					template.setUserId(userId.trim());
					sandboxRestMapper.templateUser(template);
				}
			}
			
			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			return resultJson;
		}
	}

	/**
	 * 템플릿 삭제
	 * @param templateId
	 * @throws Exception
	 */
	public void templateAsDelete(Integer templateId) throws Exception {
		// 템플릿 상태 변경(삭제)
		sandboxRestMapper.templateAsDelete(templateId);
		
		// 템플릿 사용자 삭제
		sandboxRestMapper.deleteTemplateUser(templateId);
	}

	/**
	 * 템플릿 수정
	 * @param template
	 * @return
	 */
	public JSONObject templatesAsPatch(Template template) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		// 템플릿 명 중복 체크
		Map<String, Object> detail = sandboxRestMapper.template(template.getTemplateId());
		if( !detail.get("NAME").equals(template.getName()) &&
				sandboxRestMapper.checkTemplateName(template.getName()) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			template.setDataSummaryToString(template.getDataSummary().toString());
			
			// 템플릿 수정
			sandboxRestMapper.templatesAsPatch(template);
			
			// 템플릿 사용자 넣어주기
			if( !template.isPublicFlag() ) {
				// 기존 유저 삭제
				sandboxRestMapper.deleteTemplateUser(template.getTemplateId());
				
				// 신규 유저 등록
				List<String> userList = Arrays.asList(template.getUserIdList().split(","));
				for( String userId : userList ) {
					template.setUserId(userId);
					sandboxRestMapper.templateUser(template);
				}
			}
			
			resultJson.put("result", "success");
			resultJson.put("type", "2004");
			return resultJson;
		}
	}

	/**
	 * 샌드박스 생성
	 * @param instance
	 * @throws Exception 
	 * @throws InterruptedException 
	 */
	public JSONObject instancesAsPost(Instance instance) throws Exception{

		JSONObject returnJson = new JSONObject();

		// 중복체크
		if( sandboxRestMapper.checkInstanceName(instance.getName()) > 0 ) {
			returnJson.put("result", "fail");
			returnJson.put("type", "4100");
			returnJson.put("detail", "duplicateName");
			return returnJson;

		}else {
			// 템플릿 데이터 가져오기
			Map<String, Object> templateDetail = sandboxRestMapper.template(instance.getTemplateId());

			String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX;

			//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
			Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
													.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
													.add("Content-Type", "application/json").build();

			logger.info(url);

			JSONObject paramJson = new JSONObject();
			JSONArray jsonTempArr = new JSONArray();
			JSONObject  httpResponseJson= new JSONObject();
			paramJson.put("name", instance.getName());
			paramJson.put("sourceType", "image");
			paramJson.put("volumeCreated", "false");

			paramJson.put("imageId", templateDetail.get("SNAPSHOT_ID"));
			paramJson.put("flavorName", instance.getCloudInstanceServerId());
			paramJson.put("keyPair", keyName);
			paramJson.put("zone", availabilityZone);

			jsonTempArr.add(networks);
			paramJson.put("networkId", jsonTempArr);

			jsonTempArr = new JSONArray();
			jsonTempArr.add(securityGroups);
			paramJson.put("securityGroupName", jsonTempArr);

			logger.info(paramJson.toString());

			httpResponseJson = httpService.httpServicePOST(url, headers, paramJson.toString());

			if( "201".equals(httpResponseJson.get("type")) ) {
				logger.info("Server creation completed... ");

				instance.setKeypairName(keyName); // 키페어 이름
				instance.setAvailabilityZone(availabilityZone);// 가용구역
				instance.setServerState("create_call"); // 서버상태
				instance.setModuleState("checking");
				instance.setAnalysisInstanceServerType("sandbox"); // 서버타입(sandbox, batch)

				/* 인스턴스 저장 */
				sandboxRestMapper.insertInstance(instance);
				logger.info("Instance insert completed...");

				/* 인스턴스 상세 저장 */
				instance.setDataSummaryToString(""+templateDetail.get("DATA_SUMMARY"));// 데이터 내역
				instance.setDataStartDate(""+templateDetail.get("DATA_STARTDATE"));// 데이터 시작일자
				instance.setDataEndDate(""+templateDetail.get("DATA_ENDDATE"));// 데이터 종료일자
				instance.setSnapshotId(""+templateDetail.get("SNAPSHOT_ID")); // 스냅샷 아이디

				sandboxRestMapper.insertInstanceDetail(instance);
				logger.info("InstanceDetail insert completed...");

				Map<String, Object> detail = sandboxRestMapper.instance(instance.getInstanceSequencePk());

				returnJson.put("instance", new JSONObject().fromObject(detail));
				returnJson.put("result", "success");
				returnJson.put("type", "2001");

			}else if( "400".equals(httpResponseJson.get("type")) ) { // Bad Request
				JSONObject errorJson = new JSONObject().fromObject(httpResponseJson.get("data"));
				String message = errorJson.get("title")+"";

				if( message.indexOf("disk is smaller than the minimum") > -1 ) { // 디스크가 이미지보다 작다
					returnJson.put("type", "4000");
					returnJson.put("detail", "disk is smaller than the minimum");
				}else {
					returnJson.put("type", "5000");
					returnJson.put("detail", httpResponseJson.get("data"));
				}


			}else if( "403".equals(httpResponseJson.get("type")) ) { // Forbidden
				JSONObject errorJson = new JSONObject().fromObject(httpResponseJson.get("data"));
				String message = errorJson.get("title")+"";

				if( message.indexOf("Quota exceeded for ram:") > -1 ) { // 할당 메모리 초과
					returnJson.put("type", "4005");
					returnJson.put("detail", "Quota exceeded for ram:");

				}else if( message.indexOf("Quota exceeded for cores:") > -1 ) { // 할당 코어 초과
					returnJson.put("type", "4005");
					returnJson.put("detail", "Quota exceeded for cores:");

				}else {
					returnJson.put("type", "5000");
					returnJson.put("detail", httpResponseJson.get("data"));
				}


			}else {
				returnJson.put("detail", httpResponseJson.get("data"));
				returnJson.put("type", "5000");
				logger.error("Failed to create server creation... ",httpResponseJson.toString());
			}
		}

		logger.info(returnJson.toString());

		return returnJson;


	}

	/**
	 * 샌드박스 시작/정지
	 * @param instancePk
	 * @return
	 */
	public JSONObject instanceAsPatch(int instancePk) throws Exception{

		Instance instance = new Instance();

		JSONObject jsonMessage = new JSONObject();

		String startOrStopCommand;
		JSONObject httpResponseJson = null;
		JSONObject returnJson = new JSONObject();

		instance.setInstanceSequencePk(instancePk);

		Map<String, Object> detail = sandboxRestMapper.instance(instancePk);
		if( "start_done".equals(detail.get("SERVER_STATE")) ) {
			// 중지
			startOrStopCommand=CLOUD_API_SERVER_STOP_COMMAND;
			instance.setServerState("end_call");
			instance.setModuleState("checking");

		}else if( "end_done".equals(detail.get("SERVER_STATE")) ) {
			// 시작
			startOrStopCommand=CLOUD_API_SERVER_START_COMMAND;
			instance.setServerState("start_call");
			instance.setModuleState("checking");
		}else{
			returnJson.put("type", "5000");
			returnJson.put("detail", "서버 상태 값 확인 필요");
			return returnJson;
		}


		String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX+"/"+detail.get("INSTANCE_ID")+"/"+startOrStopCommand;


		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		httpResponseJson = httpService.httpServicePOST(url, headers, jsonMessage.toString());


		if( httpResponseJson.get("type").equals("201") ) {
			sandboxRestMapper.updateInstance(instance);
			returnJson.put("result", "success");
			returnJson.put("type", "2004");
			return returnJson;
		}else {
			returnJson.put("type", "5000");
			returnJson.put("detail", httpResponseJson.toString());
			return returnJson;
		}

	}

	/**
	 * 샌드박스 삭제
	 * @param instancePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instanceAsDelete(int instancePk) throws Exception {

		JSONObject returnJson = new JSONObject();
		Instance instance = null;
		boolean checkDeleteInstance = false;
		JSONObject httpResponseJson;

		Map<String, Object> detail = sandboxRestMapper.instance(instancePk);

		String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX+"/"+detail.get("INSTANCE_ID");

		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		httpResponseJson = httpService.httpServiceDELETE(url, headers);

		// 클라우드 API에서 이미 삭제 처리되었을 경우
		if( "400".equals(httpResponseJson.get("type"))){

			JSONObject data = new JSONObject().fromObject(httpResponseJson.get("data"));
			if( (""+data.get("title")).indexOf("could not be found") > -1 )
			{

				checkDeleteInstance = true;

			}

		}

		if( "200".equals(httpResponseJson.get("type")) || checkDeleteInstance ){

			// instance 삭제(update)
			instance = new Instance();
			instance.setInstanceSequencePk(instancePk);
			instance.setDeleteFlag(true);;
			sandboxRestMapper.updateInstance(instance);

			if( "sandbox".equals(detail.get("ANALYSIS_INSTANCE_SERVER_TYPE")) ) {
				List<Map<String, Object>> projectList = projectRestMapper.projectsByinstancePk(instancePk);

				for( Map<String, Object> projectMap : projectList ) {
					// project 삭제(update) => 원본데이터 삭제(update) => 전처리 삭제(update) => 모델 삭제(update)
					projectRestService.projectAsDelete(Integer.parseInt(""+projectMap.get("PROJECT_SEQUENCE_PK")), "NoAPI");

				}

			}else{ // batch
				// 배치 신청 삭제
				List<Map<String, Object>> batchServiceRequestList = batchRestMapper.batchServiceRequestsByinstancePk(instancePk);
				for( Map<String, Object> batchServiceRequest : batchServiceRequestList ) {
					batchRestService.batchServiceRequestsDelete(Integer.parseInt(""+batchServiceRequest.get("BATCH_SERVICE_REQUEST_SEQUENCE_PK")));
				}

				// 배치 삭제
				List<Map<String, Object>> batchServiceList = batchRestMapper.batchServiceByinstancePk(instancePk);
				for( Map<String, Object> batchServiceMap : batchServiceList ) {
					batchRestService.batchServicesAsDelete(Integer.parseInt(""+batchServiceMap.get("BATCH_SERVICE_SEQUENCE_PK")));
				}
			}

			returnJson.put("result", "success");
			returnJson.put("type", "2001");
		}
		return returnJson;


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
			String listUrl = ip + "/localFiles?path=/"+localFile+"&&command=get_sample";
			
			localFileJson = httpService.httpServiceGET(listUrl, "");
			
			resultJson = new JSONObject();
			resultJson.put("localFile", localFileJson.get("data"));
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


	public void checkServerStateFromCloudApi() throws Exception {

		String serverState = "_call";
		List<Map<String, Object>> serverList = sandboxRestMapper.InstancesOfServerState(serverState);

		if( serverList.size() > 0 ) {
			//서버 상세조회
			JSONObject httpResponseJson;

			String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX;

			//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
			Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
													.add(CLOUD_API_AUTHORIZATION_KEY, cloudApiAuthorization)
													.add("Content-Type", "application/json").build();

			httpResponseJson = httpService.httpServiceGET(url, headers);

			JSONArray serverArrayJson =  null;

			if( "200".equals(httpResponseJson.get("type")) ) {
				logger.info("####### instanceSynchronization Completing server infomation... #######");
				Instance instance = null;
				JSONArray adressArrayJson=null;
				JSONObject adressJson = null;
				JSONObject serverJson = null;
				serverArrayJson = new JSONArray().fromObject(httpResponseJson.get("data"));

				for (int i = 0; i < serverArrayJson.size(); i++) {
					serverJson = new JSONObject().fromObject(serverArrayJson.get(i));

					for( Map<String, Object> instanceMap : serverList ) {

						// 이름이 같고 서버상태가 다를때
						if( instanceMap.get("NAME").equals(serverJson.get("name"))
								&& !instanceMap.get("SERVER_STATE").equals(serverJson.get("serverState")) ) {

							instance = new Instance();
							instance.setInstanceSequencePk(Integer.parseInt(""+instanceMap.get("INSTANCE_SEQUENCE_PK")));
							instance.setInstanceId(""+serverJson.get("id"));
							/*
							 * 서버상태
							 * create_call : 생성요청 / create_fail : 생성실패 / create_done : 생성완료 /
							 * start_call : 시작요청 / start_fail : 시작실패 / start_done : 시작완료 /
							 * end_call : 종료요청 / end_fail : 종료실패 / end_done : 종료완료
							 */

							switch (""+serverJson.get("serverState")) {
								case "running":
									if( "start_call".equals(instanceMap.get("SERVER_STATE"))
											|| "create_call".equals(instanceMap.get("SERVER_STATE")) ) {
										instance.setServerState("start_done"); // 시작완료
									}
									break;
								case "stopped":
									if( "end_call".equals(instanceMap.get("SERVER_STATE")) ) {
										instance.setServerState("end_done"); // 종료완료
										instance.setModuleState("server_end"); // 모듈상태 종료
									}
									break;
								case "pending" :
									break;
								default:
									logger.info("등록되지 않은 서버상태 : "+serverJson.get("serverState"));
									break;
							}


							// 내부 IP, 외부 IP  addresses
							if( MakeUtil.isNotNullAndEmpty(serverJson.get("addresses")) &&
									instanceMap.get("PRIVATE_IP") == null || instanceMap.get("PRIVATE_IP") == "" ){

								adressArrayJson = new JSONArray().fromObject(serverJson.get("addresses"));
								for(int j=0;j<adressArrayJson.size();j++) {
									adressJson = new JSONObject().fromObject(adressArrayJson.get(j));
									String ipAddress = adressJson.get("addr").toString();

									for (SubnetUtils publicSubnetUtil : publicSubnetUtils) {
										if(publicSubnetUtil.getInfo().isInRange(ipAddress)){
											instance.setPublicIp(ipAddress);
										}
									}

									for (SubnetUtils privateSubnetUtil : privateSubnetUtils) {
										if(privateSubnetUtil.getInfo().isInRange(ipAddress)){
											instance.setPrivateIp(ipAddress);
										}
									}
								}
								logger.info("####### instanceSynchronization addresses Info : "+serverJson.get("addresses").toString()+" #######");
							}

							// 인스턴스 상태, IP 변경
							sandboxRestMapper.updateInstance(instance);
							logger.info("####### instanceSynchronization instance Change State... "+instance.toString()+" #######");
						}
					}
				}
			}
		}else {
			logger.info("####### instanceSynchronization instance have nothing to change serverState... #######");
		}
	}
}
