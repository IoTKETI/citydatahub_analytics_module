package com.vaiv.analyticsManager.apiGw.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.apiGw.domain.OriginalDataGw;
import com.vaiv.analyticsManager.apiGw.domain.ProjectGw;
import com.vaiv.analyticsManager.apiGw.service.ProjectGwService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;

import net.sf.json.JSONObject;

@RestController
public class ProjectGwController {

	@Autowired
	private ProjectGwService projectGwService;

	@Autowired
	private RestFullReturnService restFullReturnService;
	
	/**
	 * 프로젝트 리스트 조회 API
	 * @return
	 */
	@GetMapping(value="/projects")
    public ResponseEntity<Object> projectsGw(HttpSession session) {
        try {
        	return new ResponseEntity<Object>(projectGwService.projectsGw(session),HttpStatus.OK);
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("projectsGw",e);
		}
    }

	
	/**
	 * 프로젝트 개별 조회 API
	 * @param id
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<Object> projectGw(@PathVariable String projectSequencePk){
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.projectGw(projectSequencePk);
			return restFullReturnService.restReturn(result, null);
    		
            	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("projectGw",e);
		}
	}
	
	/**
	 * 프로젝트 등록
	 * @param project
	 * @return
	 */
	@PostMapping(value="/projects")
	public ResponseEntity<Object> projectsAsPostGw(@RequestBody ProjectGw projectGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = projectGwService.projectsAsPostGw(session, projectGw);
			return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("projectsAsPostGw",e);
		}
	}
	
	/**
	 * 프로젝트 수정
	 * @param project
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<Object> projectsAsPatchGw(@PathVariable String projectSequencePk, @RequestBody ProjectGw projectGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = projectGwService.projectsAsPatchGw(session, projectSequencePk, projectGw);
			return restFullReturnService.restReturn(result, "ok");
		
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("projectsAsPatchGw",e);
		}
	}
	
	/**
	 * 프로젝트 삭제
	 * @param projectSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<Object> projectAsDeleteGw(@PathVariable String projectSequencePk, HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.projectAsDeleteGw(session, projectSequencePk, null);
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("projectAsDeleteGw",e);
		}
	}
	
	
	

	/**
	 * 원본 데이터 조회 API
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/originalData")
    public ResponseEntity<Object> originalDataListGw(@PathVariable String projectSequencePk) {
		JSONObject result = new JSONObject();
        try {
        	result = projectGwService.originalDataListGw(projectSequencePk);
        	if( "Bad Request Data".equals(result.get("title")) ){
        		return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
        	}else {
        		return new ResponseEntity<Object>(result.get("originalDataList"),HttpStatus.OK);	
        	}
        	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("originalDataListGw",e);
		}
    }
	
	/**
	 * 원본 데이터 개별 조회 API
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<Object> originalDataGw(@PathVariable String projectSequencePk, @PathVariable String originalDataSequencePk){
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.originalDataGw(projectSequencePk, originalDataSequencePk);
			return restFullReturnService.restReturn(result, null);
            	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("originalDataGw",e);
		}
	}
	
	/**
	 * 원본 데이터 생성
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PostMapping(value="/projects/{projectId}/originalData")
	public ResponseEntity<Object> originalDataAsPostGw(@PathVariable String projectId, @RequestBody OriginalDataGw originalDataGw
													,HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = projectGwService.originalDataAsPostGw(projectId, originalDataGw, session);
			return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("originalDataAsPostGw",e);
		}
	}
	
	/**
	 * 원본 데이터 전처리 테스트
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PatchMapping(value="/projects/{projectId}/originalData/{originalDataId}")
	public ResponseEntity<Object>originalDataAsPatchGw(@PathVariable String projectId, @PathVariable String originalDataId
													 , @RequestBody Map<String, Object> requestTtest, HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.originalDataAsPatchGw(projectId, originalDataId, requestTtest, session);
        	return new ResponseEntity<Object>(result,HttpStatus.OK);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("originalDataAsPatchGw",e);
		}
	}
	
	/**
	 * 원본 데이터 삭제
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<Object> originalDataAsDeleteGw(@PathVariable String projectSequencePk
													, @PathVariable String originalDataSequencePk, HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.originalDataAsDeleteGw(session, projectSequencePk, originalDataSequencePk, null);
        	return new ResponseEntity<Object>(result,HttpStatus.OK);
		
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("originalDataAsDeleteGw",e);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 전처리 처리방식 목록 가져오기 API
	 * @return
	 */
	@GetMapping(value="/preprocessFunctions")
    public ResponseEntity<Object> preprocessFunctionListGw() {
        try {
        	return new ResponseEntity<Object>(projectGwService.preprocessFunctionListGw(),HttpStatus.OK);
        	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessFunctionListGw",e);
		}
    }
	
	/**
	 * 전처리 처리방식 가져오기 API
	 * @param preprocessFunctionSequencePk
	 * @return
	 */
	@GetMapping(value="/preprocessFunctions/{preprocessFunctionSequencePk}")
	public ResponseEntity<Object> preprocessFunctionGw(@PathVariable String preprocessFunctionSequencePk){
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.preprocessFunctionGw(preprocessFunctionSequencePk);
			return restFullReturnService.restReturn(result, null);
        	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessFunctionGw",e);
		}
	}
	
	
	
	
	
	/**
	 * 전처리 데이터 목록 가져오기 API
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/{instancePk}/originalData/{originalDataSequencePk}/preprocessedData")
    public ResponseEntity<Object> preprocessedDataListGw(@PathVariable String instancePk, @PathVariable String originalDataSequencePk) {
		JSONObject result = new JSONObject();
        try {
        	result = projectGwService.preprocessedDataListGw(instancePk, originalDataSequencePk);
        	if( "Bad Request Data".equals(result.get("title")) ){
        		return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
        	}else {
        		return new ResponseEntity<Object>(result.get("preprocessedDataList"),HttpStatus.OK);	
        	}
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessedDataListGw",e);
		}
    }
	
	/**
	 * 전처리 데이터 개별 조회 API
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value="/{instancePk}/originalData/{originalDataSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<Object> preprocessedDataGw(@PathVariable String instancePk, 
			@PathVariable String originalDataSequencePk, @PathVariable String preprocessedDataSequencePk){
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.preprocessedDataGw(instancePk, originalDataSequencePk, preprocessedDataSequencePk);
			return restFullReturnService.restReturn(result, null);
        	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessedDataGw",e);
		}
	}
	
	
	/**
	 * 전처리 생성
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @param requestTtest
	 * @return
	 */
	@PostMapping(value="/projects/{projectSequencePk}/preprocessedData")
	public ResponseEntity<Object>preprocessedDataAsPostGw(HttpSession session, @PathVariable String projectSequencePk, @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		
		try {
			result = projectGwService.preprocessedDataAsPostGw(session, projectSequencePk, params);
        	return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessedDataAsPostGw",e);
		}
	}

	
	
	/**
	 * 전처리 데이터 삭제
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<Object> preprocessedDataAsDeleteGw(HttpSession session, @PathVariable String projectSequencePk, @PathVariable String preprocessedDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.preprocessedDataAsDeleteGw(session, projectSequencePk, preprocessedDataSequencePk, null);
        	return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("preprocessedDataAsDeleteGw",e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * 모델 목록 조회 API
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/models")
    public ResponseEntity<Object> modelsListGw(@PathVariable String projectSequencePk
			, @RequestParam(value="preprocessedDataSequencePk", required = false) String preprocessedDataSequencePk) {
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.modelsListGw(projectSequencePk, preprocessedDataSequencePk);
			if( "Bad Request Data".equals(result.get("title")) ){
        		return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
        	}else {
        		return new ResponseEntity<Object>(result.get("modelsList"),HttpStatus.OK);	
        	}
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelsListGw",e);
		}
    }
	
	/**
	 * 모델 개별 조회 API
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<Object> modelGw(@PathVariable String projectSequencePk, @PathVariable String modelSequencePk){
		JSONObject result = new JSONObject();
        try {
			result = projectGwService.modelGw(projectSequencePk, modelSequencePk);
			
    		if( "Bad Request Data".equals(result.get("title")) ){
				return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
    		}else if( "Resource Not Found".equals(result.get("title")) ){
				return new ResponseEntity<Object>(result,HttpStatus.NOT_FOUND);
    		}else {
    			return new ResponseEntity<Object>(result,HttpStatus.OK);
    		}
            	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelGw",e);
		}
	}
	
	/**
	 * 모델 생성
	 * @param projectSequencePk
	 * @param params
	 * @return
	 */
	@PostMapping(value="/projects/{projectSequencePk}/models")
	public ResponseEntity<Object>modelsAsPostGw(HttpSession session, @PathVariable String projectSequencePk, @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		
		try {
			result = projectGwService.modelsAsPostGw(session, projectSequencePk, params);
			return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelsAsPostGw",e);
		}
	}
	
	/**
	 * 학습 중지, 모델재생성 을 포함한 모델 수정
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<Object> modelsAsPatchGw(HttpSession session, @PathVariable String projectSequencePk
				, @PathVariable String modelSequencePk,  @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.modelsAsPatchGw(session, projectSequencePk, modelSequencePk, params);
			return restFullReturnService.restReturn(result, null);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelsAsPatchGw",e);
		}
	}
	
	/**
	 * 모델 테스트
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}/modelsTest/{modelSequencePk}")
	public ResponseEntity<Object> modelsTestAsPatchGw(HttpSession session, @PathVariable String projectSequencePk
									, @PathVariable String modelSequencePk,  @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.modelsTestAsPatchGw(session, projectSequencePk, modelSequencePk, params);
			return restFullReturnService.restReturn(result, null);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelsAsPatchGw",e);
		}
	}
	
	
	/**
	 * 모델 삭제
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<Object> modelsAsDeleteGw(HttpSession session, @PathVariable String projectSequencePk, @PathVariable String modelSequencePk){
		JSONObject result = new JSONObject();
		try {
			result = projectGwService.modelsAsDeleteGw(session, projectSequencePk, modelSequencePk, null);
        	return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("modelsAsPatchGw",e);
		}
	}
	
}
	