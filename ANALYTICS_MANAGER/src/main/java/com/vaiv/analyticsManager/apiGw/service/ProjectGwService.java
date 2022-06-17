package com.vaiv.analyticsManager.apiGw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.apiGw.domain.ModelGw;
import com.vaiv.analyticsManager.apiGw.domain.OriginalDataGw;
import com.vaiv.analyticsManager.apiGw.domain.PreprocessedDataGw;
import com.vaiv.analyticsManager.apiGw.domain.ProjectGw;
import com.vaiv.analyticsManager.apiGw.mapper.ProjectGwMapper;
import com.vaiv.analyticsManager.apiGw.mapper.SandboxGwMapper;
import com.vaiv.analyticsManager.common.service.AsyncService;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ProjectGwService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProjectGwMapper projectGwMapper;
	
	@Autowired
	private RestFullReturnService restFullReturnService;
	
	@Autowired
	private SandboxGwService sandboxGwService;
	
	@Autowired
	private SandboxGwMapper sandboxGwMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private AsyncService asyncService;
	
	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;

	/**
	 * 프로젝트 리스트 조회 API
	 * @param session
	 * @return
	 */
	public JSONArray projectsGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = projectGwMapper.projectsGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}

	/**
	 * 프로젝트 개별 조회 API
	 * @param projectSequencePk
	 * @return
	 */
	public JSONObject projectGw(String projectSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		int id;
		try {
			id = Integer.parseInt(projectSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.projectGw(id);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}
	
	/**
	 * 프로젝트 등록
	 * @param session
	 * @param project
	 * @return
	 */
	public JSONObject projectsAsPostGw(HttpSession session, ProjectGw projectGw) throws Exception {
		JSONObject result = new JSONObject();
		
		// 프로젝트 명 중복 체크
		if( projectGwMapper.checkProjectNameGw(projectGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
			
		}else {
			Map<String, Object> detail = sandboxGwMapper.instanceGw(projectGw.getInstanceId());
			if( detail == null ) {
				return restFullReturnService.resourceNotFound("Not found instanceId");
			}
			
			// 프로젝트 등록
			projectGw.setUserId(""+session.getAttribute("userId"));
			projectGwMapper.insertProjectGw(projectGw);
			return result;
		}
	}

	/**
	 * 프로젝트 수정
	 * @param session
	 * @param project
	 * @return
	 */
	public JSONObject projectsAsPatchGw(HttpSession session, String projectSequencePk, ProjectGw projectGw) throws Exception {
		JSONObject result = new JSONObject();
		
		/* 파라미터 체크 */
		int id;
		try {
			id = Integer.parseInt(projectSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.projectGw(id);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		/* 프로젝트명 중복 체크 */
		projectGw.setProjectSequencePk(id);
		if( projectGwMapper.checkProjectNameGw(projectGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
			
		}
		// 프로젝트 수정
		projectGwMapper.updateProjectGw(projectGw);
		return result;			
	}

	/**
	 * 프로젝트 삭제
	 * @param session
	 * @param projectSequencePk
	 * @param object
	 * @return
	 */
	public JSONObject projectAsDeleteGw(HttpSession session, String projectSequencePk, String option) throws Exception {
		JSONObject result = new JSONObject();
		
		/* 파라미터 체크 */
		int id;
		try {
			id = Integer.parseInt(projectSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.projectGw(id);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		

		// 원본데이터 삭제(전처리 삭제,모델 삭제) => 전처리 삭제(update) => 모델 삭제(update)
		List<Map<String, Object>> originalDataList = projectGwMapper.originalDataListGw(id);
		JSONObject originalDataJson = null;
		for( Map<String, Object> originalDataGw : originalDataList ) {
			originalDataJson = originalDataAsDeleteGw(session, ""+id, ""+originalDataGw.get("id"), option);
			if( !"success".equals(originalDataJson.get("result")) ) {
				result.put("type", "http://citydatahub.kr/errors/InternalError");
				result.put("title", "Internal Error");
				result.put("detail", originalDataJson.get("data"));
				throw new RuntimeException(result.toString());
			}
		}
		
		// 프로젝트 수정
		ProjectGw projectGw = new ProjectGw();
		projectGw.setProjectSequencePk(id);
		projectGw.setDeleteFlag(true);	
		projectGwMapper.updateProjectGw(projectGw);
		
		return result;
	}

	/**
	 * 원본 데이터 조회 API
	 * @param projectId
	 * @return
	 */
	public JSONObject originalDataListGw(String projectId) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int id;
		try {
			id = Integer.parseInt(projectId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		List<Map<String, Object>> list = projectGwMapper.originalDataListGw(id);
		
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(map));
				convertCamelJson.put("sampleData", convertSampleData(convertCamelJson.get("sampleData")));
				jsonArr.add(convertCamelJson);
			}
		}
		
		result.put("originalDataList", jsonArr);
		return result;
	}

	/**
	 * 원본 데이터 개별 조회 API
	 * @param projectId
	 * @param originalDataId
	 * @return
	 */
	public JSONObject originalDataGw(String projectSequencePk, String originalDataSequencePk) throws Exception {
		int projectSequenceId, originalDataSequenceId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
			originalDataSequenceId = Integer.parseInt(originalDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.originalDataGw(projectSequenceId, originalDataSequenceId);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(detail));
			convertCamelJson.put("sampleData", convertSampleData(convertCamelJson.get("sampleData")));
			return convertCamelJson;

		}else {
			return restFullReturnService.resourceNotFound("Not found originalData");
		}
	}
	
	
	/**
	 * 원본 데이터 생성
	 * @param originalData
	 * @return
	 */
	public JSONObject originalDataAsPostGw(String projectId, OriginalDataGw originalDataGw, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject originalDataJson = new JSONObject();
		JSONObject param = new JSONObject();
		
		int id;
		try {
			id = Integer.parseInt(projectId);
			originalDataGw.setProjectId(id);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.projectGw(id);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		// 원본리스트 중복체크
		if( projectGwMapper.checkDuplicateOriginalDataGw(originalDataGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
		}
		
		// 인스턴스 내부IP 가져오기
		String ip = sandboxGwService.getInstanceIp(originalDataGw.getInstanceId());
		String listUrl = ip + "/originalData";
		
		param.put("data_path", originalDataGw.getFilename());
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		
		if( "201".equals(httpJson.get("type")) ) {
			// 생성 성공
			originalDataJson = JSONObject.fromObject(httpJson.get("data"));
			originalDataGw.setOriginalDataId(Integer.parseInt(""+originalDataJson.get("ORIGINAL_DATA_SEQUENCE_PK")));
			originalDataGw.setName(""+originalDataJson.get("NAME"));
			originalDataGw.setFilename(""+originalDataJson.get("FILENAME"));
			originalDataGw.setFilepath(""+originalDataJson.get("FILEPATH"));
			originalDataGw.setExtension(""+originalDataJson.get("EXTENSION"));
			originalDataGw.setCreateDatetime(""+originalDataJson.get("CREATE_DATETIME"));
			originalDataGw.setColumns(""+originalDataJson.get("COLUMNS"));
			originalDataGw.setStatistics(""+originalDataJson.get("STATISTICS"));
			originalDataGw.setSampleData(""+originalDataJson.get("SAMPLE_DATA"));
			originalDataGw.setAmount(Integer.parseInt(""+originalDataJson.get("AMOUNT")));

			projectGwMapper.insertOriginalDataGw(originalDataGw);
			
		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("File Not Found");
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}

	/**
	 * 원본 데이터 전처리 테스트
	 * @param originalData
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalDataAsPatchGw(String projectId, String originalDataId, Map<String, Object> requestTtest, HttpSession session) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = new JSONObject();
		
		int projectSequenceId, originalDataSequenceId;
		try {
			projectSequenceId = Integer.parseInt(projectId);
			originalDataSequenceId = Integer.parseInt(originalDataId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 프로젝트 체크 */
		Map<String, Object> detail = projectGwMapper.projectGw(projectSequenceId);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		/* 원본데이트 체크 */
		Map<String, Object> originalDataDetail = projectGwMapper.originalDataGw(projectSequenceId, originalDataSequenceId);
		if( MakeUtil.isNotNullAndEmpty(originalDataDetail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(originalDataDetail.get("creatorId")) ) {				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));

				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		
		String listUrl = ip + "/originalData/"+originalDataSequenceId;
		
		param.put("request_test", requestTtest.get("requestTest"));
		httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
		if( "200".equals(httpJson.get("type")) ) {
		
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("The requested resource not found");
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}

	/**
	 * 원본 데이터 삭제
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalDataAsDeleteGw(HttpSession session, String projectSequencePk, String originalDataSequencePk, String option) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		int projectSequenceId, originalDataSequenceId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
			originalDataSequenceId = Integer.parseInt(originalDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 프로젝트 체크 */
		Map<String, Object> detail = projectGwMapper.projectGw(projectSequenceId);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		/* 원본데이트 체크 */
		Map<String, Object> originalDataDetail = projectGwMapper.originalDataGw(projectSequenceId, originalDataSequenceId);
		if( MakeUtil.isNotNullAndEmpty(originalDataDetail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(originalDataDetail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		// 모델정보 가져오기
		Map<String, Object> projectDetail = projectGwMapper.projectGw(projectSequenceId);
		
		// 전처리 삭제(모델 삭제) => 모델 삭제(update)
		List<Map<String, Object>> preprocessedList = projectGwMapper.preprocessedDataListGw(Integer.parseInt(""+projectDetail.get("SELECTED_INSTANCE")), originalDataSequenceId);
		
		JSONObject preprocessedJson = null;
		for( Map<String, Object> preData : preprocessedList ) {
			preprocessedJson = preprocessedDataAsDeleteGw(session, ""+projectSequenceId, ""+preData.get("PREPROCESSED_DATA_SEQUENCE_PK"), option);
			if( !"success".equals(preprocessedJson.get("result")) ) {
				result.put("type", "http://citydatahub.kr/errors/InternalError");
				result.put("title", "Internal Error");
				result.put("detail", preprocessedJson.get("data"));
			}
		}
					
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/originalData/"+originalDataSequencePk;
		
		// 원본데이터 삭제 API
		if( !"NoAPI".equals(option)) {
			httpJson = httpService.httpServiceDELETE(listUrl);
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 삭제 성공
			OriginalDataGw originalDataGw = new OriginalDataGw();
			originalDataGw.setOriginalDataId(originalDataSequenceId);
			originalDataGw.setDeleteFlag(true);
			projectGwMapper.deleteOriginalDataGw(originalDataGw);
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				// 삭제 성공
				OriginalDataGw originalDataGw = new OriginalDataGw();
				originalDataGw.setOriginalDataId(originalDataSequenceId);
				originalDataGw.setDeleteFlag(true);
				projectGwMapper.deleteOriginalDataGw(originalDataGw);

			}else {
				return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			}		
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}
	
	/**
	 * 전처리 처리방식 목록 가져오기 API
	 * @return
	 */
	public JSONArray preprocessFunctionListGw() throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectGwMapper.preprocessFunctionListGw();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}

	/**
	 * 전처리 처리방식 가져오기 API
	 * @param preprocessFunctionSequencePk
	 * @return
	 */
	public JSONObject preprocessFunctionGw(String preprocessFunctionSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		int preprocessFunctionSequenceId;
		try {
			preprocessFunctionSequenceId = Integer.parseInt(preprocessFunctionSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.preprocessFunctionGw(preprocessFunctionSequenceId);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found preprocessFunction");
		}
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}

	/**
	 * 전처리 데이터 목록 가져오기 API
	 * @param instancePk
	 * @param originalDataSequencePk
	 * @return
	 */
	public JSONObject preprocessedDataListGw(String instancePk, String originalDataSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int instanceId, originalDataSequenceId;
		try {
			instanceId = Integer.parseInt(instancePk);
			originalDataSequenceId = Integer.parseInt(originalDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		List<Map<String, Object>> list = projectGwMapper.preprocessedDataListGw(instanceId, originalDataSequenceId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				map.put("summary", MakeUtil.replaceNone(""+map.get("summary")));
				JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(map));
				jsonArr.add(convertCamelJson);
			}
		}
		
		result.put("preprocessedDataList", jsonArr);
		return result;
	}

	/**
	 * 전처리 데이터 개별 조회 API
	 * @param instancePk
	 * @param originalDataSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	public JSONObject preprocessedDataGw(String instancePk, String originalDataSequencePk,
			String preprocessedDataSequencePk) throws Exception {
		int instanceId, preprocessedDataId;
		try {
			instanceId = Integer.parseInt(instancePk);
			Integer.parseInt(originalDataSequencePk);
			preprocessedDataId = Integer.parseInt(preprocessedDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.preprocessedDataGw(instanceId, preprocessedDataId);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			detail.put("summary", MakeUtil.replaceNone(""+detail.get("summary")));
			JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(detail));
			return convertCamelJson;
		}else {
			return restFullReturnService.resourceNotFound("Not found preprocessed Data");
		}
	}
	
	
	/**
	 * 전처리 생성
	 * @param projectSequencePk
	 * @param requestTtest
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedDataAsPostGw(HttpSession session, String projectSequencePk, Map<String, Object> params) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject preprocessedDataJson = null;
		
		int projectSequenceId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		/* 프로젝트 체크 */
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/preprocessedData";
		
		Map<String, Object> newParams = new HashMap<String, Object>();
		newParams.put("original_data_sequence_pk", newParams.get("originalDataId"));
		newParams.put("request_data", newParams.get("requestData"));
		
		param = JSONObject.fromObject(newParams);
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		if( "202".equals(httpJson.get("type")) ) {
			// 생성 성공
			
			preprocessedDataJson = JSONObject.fromObject(httpJson.get("data"));
			PreprocessedDataGw pData = new PreprocessedDataGw();
			pData.setPreprocessedDataId(Integer.parseInt(""+preprocessedDataJson.get("PREPROCESSED_DATA_SEQUENCE_PK")));
			pData.setCommand(""+preprocessedDataJson.get("COMMAND"));
			pData.setName("P_"+preprocessedDataJson.get("PREPROCESSED_DATA_SEQUENCE_PK"));
			pData.setCreateDatetime(""+preprocessedDataJson.get("CREATE_DATETIME"));
			pData.setProgressState(""+preprocessedDataJson.get("PROGRESS_STATE"));
			pData.setProgressStartDatetime(""+preprocessedDataJson.get("PROGRESS_START_DATETIME"));
			pData.setOriginalDataId(Integer.parseInt(""+preprocessedDataJson.get("ORIGINAL_DATA_SEQUENCE_FK1")));
			pData.setInstanceId(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			
			projectGwMapper.insertPreprocessedDataGw(pData);
			
			listUrl = listUrl+"/"+pData.getPreprocessedDataId();
			// 비동기 조회
			asyncService.preprocessedData(listUrl, pData.getPreprocessedDataId());
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("The requested resource not found");
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}


	/**
	 * 전처리 삭제
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedDataAsDeleteGw(HttpSession session, String projectSequencePk, String preprocessedDataSequencePk, String option) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		int projectId, preprocessedDataId;
		try {
			projectId = Integer.parseInt(projectSequencePk);
			preprocessedDataId = Integer.parseInt(preprocessedDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectGwMapper.projectGw(projectId);
		/* 프로젝트 체크 */
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		
		// 모델 삭제
		ModelGw modelGw = new ModelGw();
		modelGw.setProjectId(preprocessedDataId);
		modelGw.setPreprocessedDataId(preprocessedDataId);
		List<Map<String, Object>> modelList = projectGwMapper.modelsListGw(modelGw);
		JSONObject modelJson = null;
		for( Map<String, Object> m : modelList ) {
			modelJson = modelsAsDeleteGw(session, ""+preprocessedDataId,""+m.get("MODEL_SEQUENCE_PK"), option);
			if( !"success".equals(modelJson.get("result")) ) {
				result.put("type", "http://citydatahub.kr/errors/InternalError");
				result.put("title", "Internal Error");
				result.put("detail", modelJson.get("data"));
			}
		}
		
		// PROGRESS_STATE가 success가 아니고 option이 NoAPI가 아니면 DB에서 삭제
		Map<String, Object> detail = projectGwMapper.preprocessedDataGw(Integer.parseInt(""+project.get("SELECTED_INSTANCE")), preprocessedDataId);
		/* 전처리데이터 체크 */
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(detail.get("creatorId")) ) {
				logger.info("사용자 역할 : "+userRole+" / 사용자 아이디 : "+userId+" / 프로젝트의 사용자 아이디"+detail.get("creatorId"));
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found preprocessed Data");
		}
		
		if( "success".equals(detail.get("progressState")) && !"NoAPI".equals(option)) {
			String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			String listUrl = ip + "/preprocessedData/"+preprocessedDataId;
			
			// 전처리 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 전처리 삭제 성공
			PreprocessedDataGw pData = new PreprocessedDataGw();
			pData.setDeleteFlag(true);
			pData.setPreprocessedDataId(preprocessedDataId);
			projectGwMapper.updatePreprocessedDataGw(pData);
			
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				PreprocessedDataGw pData = new PreprocessedDataGw();
				pData.setDeleteFlag(true);
				pData.setPreprocessedDataId(preprocessedDataId);
				projectGwMapper.updatePreprocessedDataGw(pData);
				
			}else {
				return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			}
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}
	

	/**
	 * 모델 목록 조회 API
	 * @param model
	 * @return
	 */
	public JSONObject modelsListGw(String projectSequencePk, String preprocessedDataSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		int projectId, preprocessedDataId = 0;
		try {
			projectId = Integer.parseInt(projectSequencePk);
			if( MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk) ) 
				preprocessedDataId = Integer.parseInt(preprocessedDataSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		ModelGw modelGw = new ModelGw();
		modelGw.setProjectId(projectId);
		
		if( MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk) )
			modelGw.setPreprocessedDataId(preprocessedDataId);
		
		List<Map<String, Object>> list = projectGwMapper.modelsListGw(modelGw);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) ) {
				map.put("trainSummary", MakeUtil.replaceNone(""+map.get("trainSummary")));
				JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(map));
				jsonArr.add(convertCamelJson);
			}
		}
		
		result.put("modelsList", jsonArr);
		return result;
	}

	/**
	 * 모델 개별 조회 API
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	public JSONObject modelGw(String projectSequencePk, String modelSequencePk) throws Exception {
		int projectId, modelId;
		try {
			projectId = Integer.parseInt(projectSequencePk);
			modelId = Integer.parseInt(modelSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = projectGwMapper.modelGw(projectId, modelId);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			detail.put("trainSummary", MakeUtil.replaceNone(""+detail.get("trainSummary")));
			JSONObject convertCamelJson = MakeUtil.convertJsonSnakeCaseKeyToCamelCaseKey(JSONObject.fromObject(detail));
			return convertCamelJson;
		}else {
			return restFullReturnService.resourceNotFound("Not found model");
		}
	}
	
	
	/**
	 * 모델 생성
	 * @param projectSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsPostGw(HttpSession session, String projectSequencePk, Map<String, Object> params) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject modelJson = null;
		
		int projectSequenceId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		/* 프로젝트 체크 */
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/models";
		
		Map<String, Object> newParams = new HashMap<String, Object>();
		newParams.put("algorithms_sequence_pk", newParams.get("algorithmsId"));
		newParams.put("train_data", newParams.get("trainData"));
		newParams.put("model_parameters", newParams.get("modelParameters"));
		newParams.put("train_parameters", newParams.get("trainParameters"));
		
		param = JSONObject.fromObject(newParams);
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		if( "202".equals(httpJson.get("type")) ) {
			// 생성 성공
			modelJson = JSONObject.fromObject(httpJson.get("data"));
			ModelGw modelGw = new ModelGw();
			modelGw.setModelId(Integer.parseInt(""+modelJson.get("MODEL_SEQUENCE_PK")));
			modelGw.setCommand(""+modelJson.get("COMMAND"));
			modelGw.setName("M_"+modelJson.get("MODEL_SEQUENCE_PK"));
			modelGw.setCreateDatetime(""+modelJson.get("CREATE_DATETIME"));
			modelGw.setProgressState(""+modelJson.get("PROGRESS_STATE"));
			modelGw.setProgressStartDatetime(""+modelJson.get("PROGRESS_START_DATETIME"));
			modelGw.setLoadState(""+modelJson.get("LOAD_STATE"));
			modelGw.setOriginalDataId(Integer.parseInt(""+modelJson.get("ORIGINAL_DATA_SEQUENCE_FK1")));
			modelGw.setPreprocessedDataId(Integer.parseInt(""+modelJson.get("PREPROCESSED_DATA_SEQUENCE_FK2")));
			modelGw.setInstanceId(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			modelGw.setProjectId(projectSequenceId);
			
			projectGwMapper.insertModelGw(modelGw);
			
			listUrl = listUrl+"/"+modelGw.getModelId();
			// 비동기 조회
			asyncService.models(listUrl, modelGw.getModelId());
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");


		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.resourceNotFound("The requested resource not found");

		}else if( "422".equals(httpJson.get("type")) ) {
			result.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
			result.put("title", "Operation Not Supported");
			result.put("detail", httpJson.get("data"));
				

		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}

	
	/**
	 * 모델 로드, 언로드, 학습 중지를 포함한 모델 수정
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsPatchGw(HttpSession session, String projectSequencePk, String modelSequencePk, Map<String, Object> params) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;
		
		int projectSequenceId, modelId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
			modelId = Integer.parseInt(modelSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		Map<String, Object> modelDetail = projectGwMapper.modelGw(projectSequenceId, modelId);
		if( modelDetail == null  ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}
		
		if( "ongoing".equals(modelDetail.get("progressState")) || "standby".equals(modelDetail.get("progressState")) ) {
			// 인스턴스 내부IP 가져오기
			String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			listUrl = ip + "/models/"+modelId;
			param = JSONObject.fromObject(params);
			httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type")) ) {
			
			ModelGw modelGw = new ModelGw();
			if( "RESTART".equals(params.get("mode")) )	modelGw.setProgressState("ongoing");
			else modelGw.setProgressState("standby");
			modelGw.setModelId(modelId);
			projectGwMapper.updateModelsGw(modelGw); // 모델 업데이트
			
			if( "RESTART".equals(params.get("mode")) ) {
				// 비동기 조회
				asyncService.models(listUrl, modelId);
			}
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.resourceNotFound("File Not Found(result/model 경로에 파일이 없는 경우)");
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}

	/**
	 * 모델 테스트
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsTestAsPatchGw(HttpSession session, String projectSequencePk, String modelSequencePk, Map<String, Object> params) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;
		
		
		int projectSequenceId, modelId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
			modelId = Integer.parseInt(modelSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		Map<String, Object> modelDetail = projectGwMapper.modelGw(projectSequenceId, modelId);
		if( modelDetail == null  ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		// 인스턴스 내부IP 가져오기
		String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		listUrl = ip + "/models/"+modelId;
		
		Map<String, Object> newParams = new HashMap<String, Object>();
		newParams.put("mode", newParams.get("mode"));
		newParams.put("test_data_path", newParams.get("testDataPath"));
		
		param = JSONObject.fromObject(params);
		
		httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
		
		if( "200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type")) ) {
			
			return JSONObject.fromObject(httpJson.get("data"));
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			return restFullReturnService.resourceNotFound("File Not Found(result/model 경로에 파일이 없는 경우)");
			
		}else if( "422".equals(httpJson.get("type")) ) {
			result.put("type", "http://citydatahub.kr/errors/OperationNotSupported");
			result.put("title", "Operation Not Supported");
			result.put("detail", httpJson.get("data"));
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}
	
	
	/**
	 * 모델 삭제
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsDeleteGw(HttpSession session, String projectSequencePk, String modelSequencePk, String option) throws Exception {
		JSONObject result = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		
		int projectSequenceId, modelId;
		try {
			projectSequenceId = Integer.parseInt(projectSequencePk);
			modelId = Integer.parseInt(modelSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(projectSequenceId);
		if( MakeUtil.isNotNullAndEmpty(project) ) {
			/* 사용자 권한체크 */
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");
			if( !"Analytics_Admin".equals(userRole) || !userId.equals(project.get("userId")) ) {
				return restFullReturnService.unauthorized("Unauthorized");
			}
		}else {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		Map<String, Object> modelDetail = projectGwMapper.modelGw(projectSequenceId, modelId);
		if( modelDetail == null  ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		if( "success".equals(modelDetail.get("progressState")) && !"NoAPI".equals(option)) {
			// 인스턴스 내부IP 가져오기
			String ip = sandboxGwService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			String listUrl = ip + "/models/"+modelId;
			
			// 모델 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 삭제 성공
			ModelGw modelGw = new ModelGw();
			modelGw.setDeleteFlag(true);
			modelGw.setModelId(modelId);
			projectGwMapper.updateModelsGw(modelGw); // 모델 업데이트
			
		}else if( "400".equals(httpJson.get("type")) ) {
			return restFullReturnService.badRequestData("Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				ModelGw modelGw = new ModelGw();
				modelGw.setDeleteFlag(true);
				modelGw.setModelId(modelId);
				projectGwMapper.updateModelsGw(modelGw); // 모델 업데이트
				
			}else {
				return restFullReturnService.resourceNotFound("File Not Found(result/model 경로에 파일이 없는 경우)");
			}
			
			
		}else {
			return restFullReturnService.internalError(httpJson.get("data"));
		}
		
		return result;
	}

	/**
	 * sampleData 변경
	 * "dayOfWeek": [{"0": "monday","1": "monday","2": "monday","3": "monday","4": "monday"}]
	 * 을 이렇게..
       "dayOfWeek": ["monday","monday","monday","monday","monday"]
	 * @param sampleData
	 * @return
	 */
	public JSONObject convertSampleData(Object sampleData) {
		JSONObject resultJson = new JSONObject();
		JSONObject sampleDataJson = JSONObject.fromObject(sampleData);
		ArrayList<String> stringArr = null;
		JSONArray valueArray = null;
		JSONObject json = null;
		String key;
		Object value;
		
		Iterator<?> it = sampleDataJson.keys();
		
		while( it.hasNext() ) {
			key = (String) it.next();
			value = sampleDataJson.get(key);
			valueArray = JSONArray.fromObject(value);
			stringArr = new ArrayList<String>();
			
			for (int i = 0; i < valueArray.size(); i++) {
				json = JSONObject.fromObject(valueArray.get(i));
				
				Iterator<?> sampleIt = json.keys();
				while( sampleIt.hasNext() ) {
					stringArr.add(""+json.get(sampleIt.next()));
				}
			}
			resultJson.put(key, stringArr);
		}
		return resultJson;
	}
	
}
