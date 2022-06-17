package com.vaiv.analyticsManager.restFullApi.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.common.service.AsyncService;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Model;
import com.vaiv.analyticsManager.restFullApi.domain.OriginalData;
import com.vaiv.analyticsManager.restFullApi.domain.PreprocessedData;
import com.vaiv.analyticsManager.restFullApi.domain.Project;
import com.vaiv.analyticsManager.restFullApi.mapper.ProjectRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ProjectRestService {

	@Autowired
	private HttpService httpService;
	
	@Autowired
	private ProjectRestMapper projectRestMapper;

	@Autowired
	private SandboxRestService sandboxRestService;
	
	@Autowired
	private AsyncService asyncService;
	
	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	/**
	 * 프로젝트 리스트 조회
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject projects(HttpSession session) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = projectRestMapper.projects(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("projects", jsonArr);
		return resultJson;
	}

	/**
	 * 프로젝트 개별 조회
	 * @param projectSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject project(Integer projectSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = projectRestMapper.project(projectSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("project", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 프로젝트 등록
	 * @param project
	 * @return
	 * @throws Exception 
	 */
	public JSONObject projectsAsPost(Project project) throws Exception {
		JSONObject resultJson = new JSONObject();
		// 프로젝트 명 중복 체크
		if( projectRestMapper.checkProjectName(project) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			// 프로젝트 등록
			if( MakeUtil.isNotNullAndEmpty(project.getProjectSequencePk()) )	projectRestMapper.updateProject(project);
			else projectRestMapper.insertProject(project);
			
			Map<String, Object> detail = projectRestMapper.project(project.getProjectSequencePk());
			if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("project", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
			
			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			return resultJson;
		}
	}

	/**
	 * 프로젝트 수정
	 * @param project
	 * @return
	 */
	public JSONObject projectsAsPatch(Integer projectSequencePk, Project project)throws Exception {
		JSONObject resultJson = new JSONObject();
		// 프로젝트 명 중복 체크
		project.setProjectSequencePk(projectSequencePk);
		if( projectRestMapper.checkProjectName(project) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			// 프로젝트 수정
			projectRestMapper.updateProject(project);
			
			Map<String, Object> detail = projectRestMapper.project(project.getProjectSequencePk());
			if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("project", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
			
			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			return resultJson;			
		}
	}

	/**
	 * 프로젝트 삭제
	 * @param projectSequencePk
	 * @return
	 */
	public JSONObject projectAsDelete(Integer projectSequencePk, String option) throws Exception {
		JSONObject resultJson = new JSONObject();

		// 원본데이터 삭제(전처리 삭제,모델 삭제) => 전처리 삭제(update) => 모델 삭제(update)
		List<Map<String, Object>> originalDataList = projectRestMapper.originalDataList(projectSequencePk);
		JSONObject originalDataJson = null;
		for( Map<String, Object> originalData : originalDataList ) {
			originalDataJson = originalDataAsDelete(projectSequencePk, Integer.parseInt(""+originalData.get("ORIGINAL_DATA_SEQUENCE_PK")), option);
			if( !"success".equals(originalDataJson.get("result")) ) {
				resultJson.put("type", originalDataJson.get("type"));
				resultJson.put("title", originalDataJson.get("title"));
				resultJson.put("data", originalDataJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}
		
		// 프로젝트 수정
		Project project = new Project();
		project.setProjectSequencePk(projectSequencePk);
		project.setDeleteFlag(true);	
		projectRestMapper.updateProject(project);
		
		resultJson.put("result", "success");
		resultJson.put("type", "2001");
		return resultJson;
	}

	/**
	 * 원본 데이터 조회
	 * @param projectSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalDataList(Integer projectSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectRestMapper.originalDataList(projectSequencePk);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("originalDataList", jsonArr);
		return resultJson;
	}

	/**
	 * 원본 데이터 개별 조회
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalData(Integer projectSequencePk, Integer originalDataSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = projectRestMapper.originalData(projectSequencePk, originalDataSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("originalData", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 원본 데이터 등록
	 * @param originalData
	 * @return
	 */
	public JSONObject originalDataAsPost(OriginalData originalData) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject originalDataJson = new JSONObject();
		JSONObject param = new JSONObject();
		
		// 원본리스트 중복체크
		if( projectRestMapper.checkDuplicateOriginalData(originalData) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}
		
		// 인스턴스 내부IP 가져오기
		String ip = sandboxRestService.getInstanceIp(originalData.getInstanceSequenceFk2());
		String listUrl = ip + "/originalData";
		
		param.put("data_path", originalData.getFilename());
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		if( "201".equals(httpJson.get("type")) ) {
			// 생성 성공
			originalDataJson = JSONObject.fromObject(httpJson.get("data"));
			originalData.setOriginalDataSequencePk(Integer.parseInt(""+originalDataJson.get("ORIGINAL_DATA_SEQUENCE_PK")));
			originalData.setName(""+originalDataJson.get("NAME"));
			originalData.setFilename(""+originalDataJson.get("FILENAME"));
			originalData.setFilepath(""+originalDataJson.get("FILEPATH"));
			originalData.setExtension(""+originalDataJson.get("EXTENSION"));
			originalData.setCreateDatetime(""+originalDataJson.get("CREATE_DATETIME"));
			originalData.setColumns(""+originalDataJson.get("COLUMNS"));
			originalData.setStatistics(""+originalDataJson.get("STATISTICS"));
			originalData.setSampleData(""+originalDataJson.get("SAMPLE_DATA"));
			if (originalDataJson.get("AMOUNT") != null){
				originalData.setAmount(Integer.parseInt(""+originalDataJson.get("AMOUNT")));
			} else {
				originalData.setAmount(0);
			}
			

			projectRestMapper.insertOriginalData(originalData);
			resultJson.put("result", "success");
			resultJson.put("type", "2000");	
			
		}else if( "404".equals(httpJson.get("type")) ) {			
			resultJson.put("type", "4101");
			resultJson.put("detail", "MANDATORY PARAMETER MISSING");
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4004");
			resultJson.put("detail", "File Not Found");
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 원본 데이터 전처리 테스트
	 * @param originalData
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalDataAsPatch(Integer projectSequencePk, Integer originalDataSequencePk, Map<String, Object> requestTtest) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = new JSONObject();
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
		String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		
		String listUrl = ip + "/originalData/"+originalDataSequencePk;
		
		param.put("request_test", requestTtest.get("request_test"));
		httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
		if( "200".equals(httpJson.get("type")) ) {
			// 테스트 성공
			resultJson.put("data", httpJson.get("data"));
			resultJson.put("result", "success");
			resultJson.put("type", "2000");	
		
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4004");
			resultJson.put("detail", "The requested resource not found");
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 원본 데이터 삭제
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject originalDataAsDelete(Integer projectSequencePk, Integer originalDataSequencePk, String option) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		// 모델정보 가져오기
		Map<String, Object> projectDetail = projectRestMapper.project(projectSequencePk);
		
		// 전처리 삭제(모델 삭제) => 모델 삭제(update)
		List<Map<String, Object>> preprocessedList = projectRestMapper.preprocessedDataList(Integer.parseInt(""+projectDetail.get("SELECTED_INSTANCE")), originalDataSequencePk);
		
		JSONObject preprocessedJson = null;
		for( Map<String, Object> preData : preprocessedList ) {
			preprocessedJson = preprocessedDataAsDelete(projectSequencePk, Integer.parseInt(""+preData.get("PREPROCESSED_DATA_SEQUENCE_PK")), option);
			if( !"success".equals(preprocessedJson.get("result")) ) {
				resultJson.put("type", preprocessedJson.get("type"));
				resultJson.put("title", preprocessedJson.get("title"));
				resultJson.put("data", preprocessedJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}
					
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
		String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/originalData/"+originalDataSequencePk;
		
		// 원본데이터 삭제 API
		if( !"NoAPI".equals(option)) {
			httpJson = httpService.httpServiceDELETE(listUrl);
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 삭제 성공
			OriginalData originalData = new OriginalData();
			originalData.setOriginalDataSequencePk(originalDataSequencePk);
			originalData.setDeleteFlag(true);
			projectRestMapper.deleteOriginalData(originalData);
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				// 삭제 성공
				OriginalData originalData = new OriginalData();
				originalData.setOriginalDataSequencePk(originalDataSequencePk);
				originalData.setDeleteFlag(true);
				projectRestMapper.deleteOriginalData(originalData);
				
				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			}else {
				resultJson.put("type", "4101");
				resultJson.put("detail", "MANDATORY PARAMETER MISSING");	
			}		
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 전처리 처리방식 목록 가져오기
	 * @param projectSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessFunctionList() throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectRestMapper.preprocessFunctionList();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("preprocessFunctionList", jsonArr);
		return resultJson;
	}

	/**
	 * 전처리 처리방식 가져오기
	 * @param preprocessFunctionSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessFunction(Integer preprocessFunctionSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = projectRestMapper.preprocessFunction(preprocessFunctionSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("preprocessFunction", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 전처리 생성
	 * @param projectSequencePk
	 * @param requestTtest
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedDataAsPost(Integer projectSequencePk, Map<String, Object> params) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject preprocessedDataJson = null;
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
		String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/preprocessedData";
		
		param = JSONObject.fromObject(params);
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		if( "202".equals(httpJson.get("type")) ) {
			// 생성 성공
			
			preprocessedDataJson = JSONObject.fromObject(httpJson.get("data"));
			PreprocessedData pData = new PreprocessedData();
			pData.setPreprocessedDataSequencePk(Integer.parseInt(""+preprocessedDataJson.get("PREPROCESSED_DATA_SEQUENCE_PK")));
			pData.setCommand(""+preprocessedDataJson.get("COMMAND"));
			pData.setName("P_"+preprocessedDataJson.get("PREPROCESSED_DATA_SEQUENCE_PK"));
			pData.setCreateDatetime(""+preprocessedDataJson.get("CREATE_DATETIME"));
			pData.setProgressState(""+preprocessedDataJson.get("PROGRESS_STATE"));
			pData.setProgressStartDatetime(""+preprocessedDataJson.get("PROGRESS_START_DATETIME"));
			pData.setOriginalDataSequenceFk1(Integer.parseInt(""+preprocessedDataJson.get("ORIGINAL_DATA_SEQUENCE_FK1")));
			pData.setInstanceSequenceFk2(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			
			projectRestMapper.insertPreprocessedData(pData);
			
			listUrl = listUrl+"/"+pData.getPreprocessedDataSequencePk();
			// 비동기 조회
			asyncService.preprocessedData(listUrl, pData.getPreprocessedDataSequencePk());
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
		
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4101");
			resultJson.put("detail", "Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4004");
			resultJson.put("detail", "The requested resource not found");
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 전처리 데이터 목록 가져오기
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedDataList(Integer instancePk, Integer originalDataSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectRestMapper.preprocessedDataList(instancePk, originalDataSequencePk);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("preprocessedDataList", jsonArr);
		return resultJson;
	}

	/**
	 * 전처리 데이터 개별 조회
	 * @param originalDataSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedData(Integer instancePk, Integer originalDataSequencePk, Integer preprocessedDataSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = projectRestMapper.preprocessedData(instancePk, preprocessedDataSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("preprocessedData", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}
	

	/**
	 * 전처리 삭제
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject preprocessedDataAsDelete(Integer projectSequencePk, Integer preprocessedDataSequencePk, String option) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		// 모델 삭제
		Model model = new Model();
		model.setProjectSequenceFk4(projectSequencePk);
		model.setPreprocessedDataSequenceFk2(preprocessedDataSequencePk);
		List<Map<String, Object>> modelList = projectRestMapper.modelsList(model);
		JSONObject modelJson = null;
		for( Map<String, Object> m : modelList ) {
			modelJson = modelsAsDelete(projectSequencePk,Integer.parseInt(""+m.get("MODEL_SEQUENCE_PK")), option);
			if( !"success".equals(modelJson.get("result")) ) {
				resultJson.put("type", modelJson.get("type"));
				resultJson.put("title", modelJson.get("title"));
				resultJson.put("data", modelJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
					
		// PROGRESS_STATE가 success가 아니고 option이 NoAPI가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.preprocessedData(Integer.parseInt(""+project.get("SELECTED_INSTANCE")), preprocessedDataSequencePk);
		if( "success".equals(detail.get("PROGRESS_STATE")) && !"NoAPI".equals(option)) {
			String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			String listUrl = ip + "/preprocessedData/"+preprocessedDataSequencePk;
			
			// 전처리 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 전처리 삭제 성공
			PreprocessedData pData = new PreprocessedData();
			pData.setDeleteFlag(true);
			pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
			projectRestMapper.updatePreprocessedData(pData);
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");	
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				PreprocessedData pData = new PreprocessedData();
				pData.setDeleteFlag(true);
				pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
				projectRestMapper.updatePreprocessedData(pData);
				
				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			}else {
				resultJson.put("type", "4101");
				resultJson.put("detail", "MANDATORY PARAMETER MISSING");	
			}
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 모델 생성
	 * @param projectSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsPost(Integer projectSequencePk, Map<String, Object> params) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject modelJson = null;
		
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
		String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		String listUrl = ip + "/models";
		
		param = JSONObject.fromObject(params);
		httpJson = httpService.httpServicePOST(listUrl, param.toString(), null);
		if( "202".equals(httpJson.get("type")) ) {
			// 생성 성공
			modelJson = JSONObject.fromObject(httpJson.get("data"));
			Model model = new Model();
			model.setModelSequencePk(Integer.parseInt(""+modelJson.get("MODEL_SEQUENCE_PK")));
			model.setCommand(""+modelJson.get("COMMAND"));
			model.setName("M_"+modelJson.get("MODEL_SEQUENCE_PK"));
			model.setCreateDatetime(""+modelJson.get("CREATE_DATETIME"));
			model.setProgressState(""+modelJson.get("PROGRESS_STATE"));
			model.setProgressStartDatetime(""+modelJson.get("PROGRESS_START_DATETIME"));
			model.setLoadState(""+modelJson.get("LOAD_STATE"));
			model.setOriginalDataSequenceFk1(Integer.parseInt(""+modelJson.get("ORIGINAL_DATA_SEQUENCE_FK1")));
			model.setPreprocessedDataSequenceFk2(Integer.parseInt(""+modelJson.get("PREPROCESSED_DATA_SEQUENCE_FK2")));
			model.setInstanceSequenceFk3(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			model.setProjectSequenceFk4(projectSequencePk);
			
			projectRestMapper.insertModel(model);
			
			listUrl = listUrl+"/"+model.getModelSequencePk();
			// 비동기 조회
			asyncService.models(listUrl, model.getModelSequencePk());
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
		
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("result", "error");
			resultJson.put("type", "4101");
			resultJson.put("detail", "Mandatory Parameter Missing");
			
		}else if( "404".equals(httpJson.get("type")) ) {
			resultJson.put("result", "error");
			resultJson.put("type", "4004");
			resultJson.put("detail", "The requested resource not found");
		
		}else if( "422".equals(httpJson.get("type")) ) {
			resultJson.put("result", "error");
			resultJson.put("type", "4202");
			resultJson.put("detail", httpJson.get("title"));
			resultJson.put("data", httpJson.get("data"));
			
		}else {
			resultJson.put("result", "error");
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 모델 목록 조회
	 * @param projectSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsList(Model model) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectRestMapper.modelsList(model);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("modelsList", jsonArr);
		return resultJson;
	}

	/**
	 * 모델 개별 조회
	 * @param modelSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject model(Integer projectSequencePk, Integer modelSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail.get("PROGRESS_END_DATETIME")) ) {
			String startDate = ""+detail.get("PROGRESS_START_DATETIME");
			String endDate = ""+detail.get("PROGRESS_END_DATETIME");
			startDate = startDate.substring(0,19).replaceAll("T", " ");
			endDate = endDate.substring(0,19).replaceAll("T", " ");
			detail.put("diffDateTime", MakeUtil.diffOfDateAll(startDate, endDate));
		}
		
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("model", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 모델 삭제
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsDelete(Integer projectSequencePk, Integer modelSequencePk, String option) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if( "success".equals(detail.get("PROGRESS_STATE")) && !"NoAPI".equals(option)) {
			// 인스턴스 내부IP 가져오기
			Map<String, Object> project = projectRestMapper.project(projectSequencePk);
			String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			String listUrl = ip + "/models/"+modelSequencePk;
			
			// 모델 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) ) {
			// 삭제 성공
			Model model = new Model();
			model.setDeleteFlag(true);
			model.setModelSequencePk(modelSequencePk);
			projectRestMapper.updateModels(model); // 모델 업데이트
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");	
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			JSONObject json = JSONObject.fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if( "4004".equals(json.get("type")) && "File Not Found".equals(json.get("title")) ){
				Model model = new Model();
				model.setDeleteFlag(true);
				model.setModelSequencePk(modelSequencePk);
				projectRestMapper.updateModels(model); // 모델 업데이트
				
				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			}else {
				resultJson.put("type", "4101");
				resultJson.put("detail", "File Not Found(result/model 경로에 파일이 없는 경우)");	
			}
			
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 모델 로드, 언로드, 학습 중지를 포함한 모델 수정
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsAsPatch(Integer projectSequencePk, Integer modelSequencePk, Map<String, Object> params) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if( "ongoing".equals(detail.get("PROGRESS_STATE")) || "standby".equals(detail.get("PROGRESS_STATE")) ) {
			// 인스턴스 내부IP 가져오기
			Map<String, Object> project = projectRestMapper.project(projectSequencePk);
			String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
			listUrl = ip + "/models/"+modelSequencePk;
			param = JSONObject.fromObject(params);
			httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
			
		}else {
			httpJson.put("type", "200");
		}
		
		if( "200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type")) ) {
			
			Model model = new Model();
			if( "RESTART".equals(params.get("mode")) )	model.setProgressState("ongoing");
			else model.setProgressState("standby");
			model.setModelSequencePk(modelSequencePk);
			projectRestMapper.updateModels(model); // 모델 업데이트
			
			if( "RESTART".equals(params.get("mode")) ) {
				// 비동기 조회
				asyncService.models(listUrl, modelSequencePk);
			}
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");	
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4101");
			resultJson.put("detail", "File Not Found(result/model 경로에 파일이 없는 경우)");
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 모델 테스트
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public JSONObject modelsTestAsPatch(Integer projectSequencePk, Integer modelSequencePk, Map<String, Object> params) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;
		
		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		// 인스턴스 내부IP 가져오기
		Map<String, Object> project = projectRestMapper.project(projectSequencePk);
		String ip = sandboxRestService.getInstanceIp(Integer.parseInt(""+project.get("SELECTED_INSTANCE")));
		listUrl = ip + "/models/"+modelSequencePk;
		param = JSONObject.fromObject(params);
		
		httpJson = httpService.httpServicePATCH(listUrl, param.toString(), null);
		
		if( "200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type")) ) {
			
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "400".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Bad Request");
			resultJson.put("data", httpJson.get("data"));
			
		}else if( "404".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4101");
			resultJson.put("detail", "File Not Found(result/model 경로에 파일이 없는 경우)");
			
		}else if( "422".equals(httpJson.get("type")) ) {
			resultJson.put("type", "4000");
			resultJson.put("title", "Unprocessible Entity");
			resultJson.put("data", httpJson.get("data"));
			
		}else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		
		return resultJson;
	}

	/**
	 * 모델 목록 가져오기
	 * @param userId
	 * @return
	 */
	public JSONObject modelsOfInstancePk(Integer instanceSequencePk) {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = projectRestMapper.modelsOfInstancePk(instanceSequencePk);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("modelsList", jsonArr);
		return resultJson;
	}

	
	
}
