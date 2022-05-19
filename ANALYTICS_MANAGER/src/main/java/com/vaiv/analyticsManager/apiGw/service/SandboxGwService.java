package com.vaiv.analyticsManager.apiGw.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.apiGw.domain.InstanceGw;
import com.vaiv.analyticsManager.apiGw.domain.TemplateGw;
import com.vaiv.analyticsManager.apiGw.mapper.BatchGwMapper;
import com.vaiv.analyticsManager.apiGw.mapper.ProjectGwMapper;
import com.vaiv.analyticsManager.apiGw.mapper.SandboxGwMapper;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@SuppressWarnings("static-access")
public class SandboxGwService {
	
	private static Logger logger = LoggerFactory.getLogger(SandboxGwService.class);
	
	@Autowired
	private SandboxGwMapper sandboxGwMapper;
	
	@Autowired
	private ProjectGwService projectGwService;
	
	@Autowired
	private ProjectGwMapper projectGwMapper;
	
	
	@Autowired
	private BatchGwService batchGwService;
	
	@Autowired
	private BatchGwMapper batchGwMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private RestFullReturnService restFullReturnService;


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

	private final String CLOUD_API_SERVER_POST_FIX="/openstack/infra/cloudServices/openstack/servers";

	private final String CLOUD_API_CREDENTIAL_KEY="credential";
	private final String CLOUD_API_AUTHORIZATIONV_KEY="Authorization";
	private final String CLOUD_API_SERVER_START_COMMAND="start";
	private final String CLOUD_API_SERVER_STOP_COMMAND="stop";


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
	 * 샌드박스 템플릿 신청 이력 조회 API
	 * @return
	 */
	public JSONArray customAnalysisTemplateRequestsGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxGwMapper.customAnalysisTemplateRequestsGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				JSONObject j = new JSONObject().fromObject(map.get("entities"));
				if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) map.put("entities", j.get("availableList"));
				
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}
		
		return jsonArr;
	}

	/**
	 * 샌드박스 템플릿 신청이력 개별조회 API
	 * @param templateId
	 * @return
	 */
	public JSONObject customAnalysisTemplateRequestGw(String customTemplateId) throws Exception {
		JSONObject result = new JSONObject();
		int id;
		try {
			id = Integer.parseInt(customTemplateId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		Map<String, Object> detail = sandboxGwMapper.customAnalysisTemplateRequestGw(id);
		
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			JSONObject j = new JSONObject().fromObject(detail.get("entities"));
			if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) detail.put("entities", j.get("availableList"));
		}else {
			return restFullReturnService.resourceNotFound("Not found custom template");
		}
		result = MakeUtil.nvlJson(new JSONObject().fromObject(detail));
		return result;
	}
	
	
	/**
	 * 샌드박스 템플릿 추가 요청
	 * @param template
	 * @throws Exception
	 */
	public JSONObject customAnalysisTemplateRequestsAsPostGw(TemplateGw templateGw, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		templateGw.setUserId(""+session.getAttribute("userId"));
		
		JSONObject entitiesJson = new JSONObject();
		entitiesJson.put("availableList", templateGw.getEntities().toString());
		templateGw.setDataSummaryToString(entitiesJson.toString());
		
		sandboxGwMapper.customAnalysisTemplateRequestsAsPostGw(templateGw);
		return result;
	}
	
	/**
	 * 샌드박스 템플릿  생성요청 취소 또는 커스텀 샌드박스 관리자 승인 또는 거절 또는 완료
	 * @param customTemplateId
	 * @return
	 */
	public JSONObject customAnalysisTemplateRequestsAsPatchGw(String customTemplateId, TemplateGw templateGw, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		
		if( !"Analytics_Admin".equals(session.getAttribute("userRole")) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		int id;
		try {
			id = Integer.parseInt(customTemplateId);
			templateGw.setCustomTemplateId(id);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		Map<String, Object> detail = sandboxGwMapper.customAnalysisTemplateRequestGw(id);
		
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			sandboxGwMapper.customAnalysisTemplateRequestsAsPatchGw(templateGw);
			return result;
		}else {
			return restFullReturnService.resourceNotFound("Not found custom template");
		}
	}

	/**
	 * 샌드박스 템플릿 조회 API
	 * @return
	 */
	public JSONArray analysisTemplatesGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = sandboxGwMapper.analysisTemplatesGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				JSONObject j = new JSONObject().fromObject(map.get("entities"));
				if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) map.put("entities", j.get("availableList"));
				
				String[] userIdArr = (""+map.get("userId")).split(",");
				map.put("userId", userIdArr);
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}
		
		return jsonArr;
	}

	/**
	 * 샌드박스 템플릿 상세조회 API
	 * @param templateId
	 * @return
	 */
	public JSONObject analysisTemplateGw(String templateId) throws Exception {
		JSONObject result = new JSONObject();
		int id;
		try {
			id = Integer.parseInt(templateId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		Map<String, Object> detail = sandboxGwMapper.analysisTemplateGw(id);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found template");
		}
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			JSONObject j = new JSONObject().fromObject(detail.get("entities"));
			if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) detail.put("entities", j.get("availableList"));
			
			String[] userIdArr = (""+detail.get("userId")).split(",");
			detail.put("userId", userIdArr);
		}
		
		result = MakeUtil.nvlJson(new JSONObject().fromObject(detail));
		return result;
	}
	
	
	/**
	 * 템플릿 생성
	 * @param template
	 */
	public JSONObject analysisTemplatesAsPostGw(TemplateGw templateGw, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		
		if( !"Analytics_Admin".equals(session.getAttribute("userRole")) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		// 템플릿 명 중복 체크
		if( sandboxGwMapper.checkTemplateNameGw(templateGw.getName()) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate Name");
			
		}else {
			JSONObject entitiesJson = new JSONObject();
			entitiesJson.put("availableList", templateGw.getEntities().toString());
			templateGw.setDataSummaryToString(entitiesJson.toString());
			
			// 사용자의 커스텀 템플릿 진행상태 변경
			if( MakeUtil.isNotNullAndEmpty(templateGw.getCustomTemplateId()) && 0 != templateGw.getCustomTemplateId() ) {
				templateGw.setTemplateId(templateGw.getCustomTemplateId());
				templateGw.setProgressState("done");
				templateGw.setAdminComment("생성완료");
				sandboxGwMapper.customTemplateRequestsAsPatchGw(templateGw);
			}

			// 템플릿 생성
			sandboxGwMapper.analysisTemplatesAsPostGw(templateGw);
			
			// 템플릿 사용자 넣어주기
			if( !templateGw.isPublic() ) {
				templateGw.setTemplateId(templateGw.getTemplateId());
				List<String> userList = Arrays.asList(templateGw.getUsers());
				for( String userId : userList ) {
					templateGw.setUserId(userId.trim());
					sandboxGwMapper.templateUserGw(templateGw);
				}
			}
			
			return result;
		}
	}
	
	/**
	 * 템플릿 수정
	 * @param template
	 * @return
	 */
	public JSONObject analysisTemplatesAsPatchGw(String templateId, TemplateGw templateGw, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		
		if( !"Analytics_Admin".equals(session.getAttribute("userRole")) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		int id;
		try {
			id = Integer.parseInt(templateId);
			templateGw.setTemplateId(id);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 템플릿 명 중복 체크
		Map<String, Object> detail = sandboxGwMapper.analysisTemplateGw(templateGw.getTemplateId());
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found template");
		}
		
		if( !detail.get("name").equals(templateGw.getName()) && sandboxGwMapper.checkTemplateNameGw(templateGw.getName()) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate Name");
			
		}else {
			JSONObject entitiesJson = new JSONObject();
			entitiesJson.put("availableList", templateGw.getEntities().toString());
			templateGw.setDataSummaryToString(entitiesJson.toString());
			
			// 템플릿 수정
			sandboxGwMapper.analysisTemplatesAsPatchGw(templateGw);


			// 템플릿 사용자 넣어주기
			if( !templateGw.isPublic() ) {
				// 기존 유저 삭제
				sandboxGwMapper.deleteTemplateUserGw(templateGw.getTemplateId());

				List<String> userList = Arrays.asList(templateGw.getUsers());
				for( String userId : userList ) {
					templateGw.setUserId(userId.trim());
					sandboxGwMapper.templateUserGw(templateGw);
				}

			}
			return result;
		}
	}

	/**
	 * 템플릿 삭제
	 * @param templateId
	 * @throws Exception
	 */
	public JSONObject analysisTemplatesAsDeleteGw(String templateId, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		
		if( !"Analytics_Admin".equals(session.getAttribute("userRole")) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		int id;
		try {
			id = Integer.parseInt(templateId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 템플릿 명 중복 체크
		Map<String, Object> detail = sandboxGwMapper.analysisTemplateGw(id);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found template");
		}else {
			// 템플릿 상태 변경(삭제)
			sandboxGwMapper.analysisTemplatesAsDeleteGw(id);
			
			// 템플릿 사용자 삭제
			sandboxGwMapper.deleteTemplateUserGw(id);
			
			return result;
		}
	}

	

	/**
	 * 샌드박스 리스트 조회 API
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
				JSONObject j = new JSONObject().fromObject(map.get("entities"));
				if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) map.put("entities", j.get("availableList"));
				
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}
		
		return jsonArr;
	}

	/**
	 * 샌드박스 개별 조회 API
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
			JSONObject j = new JSONObject().fromObject(detail.get("entities"));
			if( MakeUtil.isNotNullAndEmpty(j.get("availableList")) ) detail.put("entities", j.get("availableList"));
			
		}else {
			return restFullReturnService.resourceNotFound("Not found instance");
		}
		
		result = MakeUtil.nvlJson(new JSONObject().fromObject(detail));
		return result;
	}
	
	
	/**
	 * 샌드박스 생성
	 * @param instance
	 * @throws Exception 
	 * @throws InterruptedException 
	 */
	// public JSONObject instancesAsPostGw(InstanceGw instanceGw, HttpSession session) throws Exception{

	// 	JSONObject returnJson = new JSONObject();
		
	// 	// 중복체크
	// 	if( sandboxGwMapper.checkInstanceNameGw(instanceGw.getName()) > 0 ) {
	// 		return restFullReturnService.alreadyExists("duplicate Name");
			
	// 	}else {
	// 		// 템플릿 데이터 가져오기
	// 		Map<String, Object> templateDetail = sandboxGwMapper.analysisTemplateGw(instanceGw.getTemplateId());

	// 		String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX;

	// 		// 인스턴스 생성
	// 		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
	// 		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
	// 												.add(CLOUD_API_AUTHORIZATIONV_KEY, cloudApiAuthorization)
	// 												.add("Content-Type", "application/json").build();

	// 		// String jsonMessage = ""; // TODO:refactoring
	// 		// JSONObject json = new JSONObject(); // TODO:refactoring
	// 		JSONObject paramJson = new JSONObject();
	// 		JSONArray jsonTempArr = new JSONArray();
	// 		// JSONObject jsonTemp = new JSONObject(); // TODO:refactoring
	// 		JSONObject  httpResponseJson= new JSONObject();
	// 		paramJson.put("name", instanceGw.getName());
	// 		paramJson.put("sourceType", "image");
	// 		paramJson.put("volumeCreated", "false");

	// 		paramJson.put("imageId", templateDetail.get("snapshotId"));
	// 		paramJson.put("flavorName", instanceGw.getCloudInstanceServerId());
	// 		paramJson.put("keyPair", keyName);
	// 		paramJson.put("zone", availabilityZone);

	// 		jsonTempArr.add(networks);
	// 		paramJson.put("networkId", jsonTempArr);

	// 		jsonTempArr = new JSONArray();
	// 		jsonTempArr.add(securityGroups);
	// 		paramJson.put("securityGroupName", jsonTempArr);

	// 		httpResponseJson = httpService.httpServicePOST(url, headers, paramJson.toString());

	// 		if( "201".equals(httpResponseJson.get("type")) ) {
	// 			logger.info("Server creation completed... ");

	// 			instanceGw.setUserId(""+session.getAttribute("userId"));
	// 			instanceGw.setKeypairName(keyName); // 키페어 이름
	// 			instanceGw.setAvailabilityZone(availabilityZone);// 가용구역
	// 			instanceGw.setServerState("create_call"); // 서버상태
	// 			instanceGw.setModuleState("checking");
	// 			instanceGw.setAnalysisInstanceServerType("sandbox"); // 서버타입(sandbox, batch)
				
	// 			/* 인스턴스 저장 */
	// 			sandboxGwMapper.insertInstanceGw(instanceGw);
	// 			logger.info("Instance insert completed...");

	// 			/* 인스턴스 상세 저장 */
	// 			instanceGw.setDataSummaryToString(""+templateDetail.get("entities"));// 데이터 내역
	// 			instanceGw.setDataStartDate(""+templateDetail.get("dataStartDate"));// 데이터 시작일자
	// 			instanceGw.setDataEndDate(""+templateDetail.get("dataEndDate"));// 데이터 종료일자
	// 			instanceGw.setSnapshotId(""+templateDetail.get("snapshotId")); // 스냅샷 아이디
				
	// 			sandboxGwMapper.insertInstanceDetailGw(instanceGw);
	// 			logger.info("InstanceDetail insert completed...");
				
	// 		}else if( "400".equals(httpResponseJson.get("type")) ) { // Bad Request
	// 			JSONObject errorJson = new JSONObject().fromObject(returnJson.get("data"));
	// 			errorJson = new JSONObject().fromObject(errorJson.get("badRequest"));
	// 			String message = errorJson.get("message")+"";
				
	// 			if( message.indexOf("disk is smaller than the minimum") > -1 ) { // 디스크가 이미지보다 작다
	// 				returnJson.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
	// 				returnJson.put("type", "Operation Not Supported");
	// 				returnJson.put("detail", "disk is smaller than the minimum");
	// 			}else {
	// 				returnJson.put("type", "http://citydatahub.kr/errors/InternalError");
	// 				returnJson.put("title", "Internal Error");
	// 				returnJson.put("detail", httpResponseJson.get("data"));
					
	// 			}
					
			
	// 		}else if( "403".equals(httpResponseJson.get("type")) ) { // Forbidden
	// 			JSONObject errorJson = new JSONObject().fromObject(httpResponseJson.get("data"));
	// 			errorJson = new JSONObject().fromObject(errorJson.get("forbidden"));
	// 			String message = errorJson.get("message")+"";
				
	// 			if( message.indexOf("Quota exceeded for ram:") > -1 ) { // 할당 메모리 초과
	// 				returnJson.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
	// 				returnJson.put("type", "Operation Not Supported");
	// 				returnJson.put("detail", "Quota exceeded for ram:");
					
	// 			}else if( message.indexOf("Quota exceeded for cores:") > -1 ) { // 할당 코어 초과
	// 				returnJson.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
	// 				returnJson.put("type", "Operation Not Supported");
	// 				returnJson.put("detail", "Quota exceeded for cores:");
					
	// 			}else {
	// 				returnJson.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
	// 				returnJson.put("type", "Operation Not Supported");
	// 				returnJson.put("detail", httpResponseJson.get("data"));
	// 			}
					
				
	// 		}else {
	// 			returnJson.put("type", "http://citydatahub.kr/errors/InternalError");
	// 			returnJson.put("title", "Internal Error");
	// 			returnJson.put("detail", httpResponseJson.get("data"));
	// 		}
	// 	}
	// 	return returnJson;
	// }

	/**
	 * 샌드박스 시작/정지
	 * @param instancePk
	 * @return
	 */
	public JSONObject instanceAsPatchGw(String instancePk, String serverState) throws Exception{
		String startOrStopCommand=null;
		JSONObject httpResponseJson = null;
		JSONObject returnJson = new JSONObject();

		JSONObject jsonMessage = new JSONObject();
		InstanceGw instanceGw = new InstanceGw();
		
		int id;
		try {
			id = Integer.parseInt(instancePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}

		instanceGw.setInstanceId(id);
		Map<String, Object> detail = sandboxGwMapper.instanceGw(id);

		if( !MakeUtil.isNotNullAndEmpty(detail) ) {
			return restFullReturnService.resourceNotFound("Not found instance");
			
		}else if(serverState.equals("end_call")&& "start_done".equals(detail.get("serverState")) ) {
			// 중지
			startOrStopCommand=CLOUD_API_SERVER_STOP_COMMAND;
			instanceGw.setServerState("end_call");
			instanceGw.setModuleState("checking");
			
		}else if(serverState.equals("start_call")&&  "end_done".equals(detail.get("serverState")) ) {
			// 시작
			startOrStopCommand=CLOUD_API_SERVER_START_COMMAND;
			instanceGw.setServerState("start_call");
			instanceGw.setModuleState("checking");
		}

		String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX+"/"+detail.get("cloudInstanceId")+"/"+startOrStopCommand;


		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATIONV_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		httpResponseJson = httpService.httpServicePOST(url, headers, jsonMessage.toString());

		
		if( "201".equals(httpResponseJson.get("type")) ){
			// instance update
			sandboxGwMapper.updateInstanceGw(instanceGw);
		}else {
			return restFullReturnService.internalError("Cloud Api Request Result is not 201");
		}
		
		return returnJson;
	}

	/**
	 * 샌드박스 삭제
	 * @param instancePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instanceAsDeleteGw(HttpSession session, String instancePk) throws Exception {
		JSONObject returnJson = new JSONObject();
		JSONObject httpResponseJson;
		InstanceGw instanceGw = null;
		boolean checkDeleteInstance = false;
		
		int id;
		try {
			id = Integer.parseInt(instancePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = sandboxGwMapper.instanceGw(id);
		if( !MakeUtil.isNotNullAndEmpty(detail) ) {
			return restFullReturnService.resourceNotFound("Not found instance");
		}

		String url = cloudApiUrl+CLOUD_API_SERVER_POST_FIX+"/"+detail.get("cloudInstanceId");

		//Headers headers=new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential).build();
		Headers headers = new Headers.Builder().add(CLOUD_API_CREDENTIAL_KEY, cloudApiCredential)
												.add(CLOUD_API_AUTHORIZATIONV_KEY, cloudApiAuthorization)
												.add("Content-Type", "application/json").build();

		httpResponseJson = httpService.httpServiceDELETE(url, headers);

		// openstact에서 이미 삭제 처리되었을 경우
		if( "400".equals(httpResponseJson.get("type"))){
			JSONObject data = new JSONObject().fromObject(httpResponseJson.get("data"));
			if( (""+data.get("title").toString()).indexOf("could not be found") > -1 )
				checkDeleteInstance = true;
		}

		if( "200".equals(httpResponseJson.get("type")) || checkDeleteInstance ){
			// instance 삭제(update)
			instanceGw = new InstanceGw();
			instanceGw.setInstanceId(id);
			instanceGw.setDeleteFlag(true);;
			sandboxGwMapper.updateInstanceGw(instanceGw);
			
			if( "sandbox".equals(detail.get("analysisInstanceServerType")) ) {
				List<Map<String, Object>> projectList = projectGwMapper.projectsByinstanceIdGw(id);
				
				for( Map<String, Object> projectMap : projectList ) {
					// project 삭제(update) => 원본데이터 삭제(update) => 전처리 삭제(update) => 모델 삭제(update)
					projectGwService.projectAsDeleteGw(session, ""+projectMap.get("PROJECT_SEQUENCE_PK"), "NoAPI");
					
				}
				
			}else{ // batch
				// 배치 신청 삭제
				List<Map<String, Object>> batchServiceRequestList = batchGwMapper.batchServiceRequestsByinstanceIdGw(id);
				for( Map<String, Object> batchServiceRequest : batchServiceRequestList ) {
					batchGwService.batchServiceRequestsDeleteGw(session, ""+batchServiceRequest.get("BATCH_SERVICE_REQUEST_SEQUENCE_PK"));	
				}
				
				// 배치 삭제
				List<Map<String, Object>> batchServiceList = batchGwMapper.batchServiceByinstanceIdGw(id);
				for( Map<String, Object> batchServiceMap : batchServiceList ) {
					batchGwService.batchServicesAsDeleteGw(session, ""+batchServiceMap.get("BATCH_SERVICE_SEQUENCE_PK"));
				}				
			}
		}
		return returnJson;
	}
	
	/**
	 * 샌드박스 로컬파일 조회 API
	 * @param selectedInstance
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFilesGw(Integer selectedInstance) throws Exception {
		JSONObject httpResult = new JSONObject();
		JSONObject result = new JSONObject();
		JSONArray fileListJson = null;
		
		// 인스턴스가 있는지 확인
		// Map<String, Object> detail = sandboxGwMapper.instanceGw(selectedInstance);
		// if( detail == null ) {
		// 	return restFullReturnService.resourceNotFound("Not found instance");
		// }
		
		// 실행중인지 확인
		// if( "start_done".equals(detail.get("SERVER_STATE")) ) {
			// 인스턴스 내부IP 가져오기
			String ip = getInstanceIp(selectedInstance);
			String listUrl = ip + moduleLocalFiles;
			httpResult = httpService.httpServiceGET(listUrl, "");
			//{"type":"200","title":"OK","data":{"command":"get_list","pathhttpResultult":{}}}
			httpResult = new JSONObject().fromObject(httpResult.get("data"));
			if( MakeUtil.isNotNullAndEmpty(httpResult.get("result")) )
				httpResult = new JSONObject().fromObject(httpResult.get("result"));
			if( MakeUtil.isNotNullAndEmpty(httpResult.get("file_list")) )
				fileListJson = new JSONArray().fromObject(httpResult.get("file_list"));
			
			result.put("localFiles", fileListJson);
			return result;
			
		// }else {
		// 	result.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
		// 	result.put("title", "Operation Not Supported");
		// 	result.put("detail", "Instance Stop Status");
			// return result;
		// }
	}

	/**
	 * 샌드박스 로컬파일 샘플 조회 API
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 * @throws Exception 
	 */
	public JSONObject instancesLocalFileSampleGw(Integer selectedInstance, String localFile) throws Exception {
		JSONObject httpResult = new JSONObject();
		JSONObject result = new JSONObject();
		
		// 인스턴스가 있는지 확인
		// Map<String, Object> detail = sandboxGwMapper.instanceGw(selectedInstance);
		// if( detail == null ) {
		// 	return restFullReturnService.resourceNotFound("Not found instance");
		// }
		
		// 실행중인지 확인
		// if( "start_done".equals(detail.get("SERVER_STATE")) ) {
			// 인스턴스 내부IP 가져오기
			String ip = getInstanceIp(selectedInstance);
			String listUrl = ip + "/localFiles?path=/"+localFile+"&&command=get_sample";
			
			httpResult = httpService.httpServiceGET(listUrl, "");
			
			result.put("localFile", httpResult.get("data"));
			return result;
			
		// }else {
		// 	result.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
		// 	result.put("title", "Operation Not Supported");
		// 	result.put("detail", "Instance Stop Status");
		// 	return result;
		// }
	}
	
	
	/**
	 * 샌드박스 내부IP 가져오기
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
