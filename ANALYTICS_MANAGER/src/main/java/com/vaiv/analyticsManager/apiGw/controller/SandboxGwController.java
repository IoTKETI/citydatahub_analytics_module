package com.vaiv.analyticsManager.apiGw.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.apiGw.domain.InstanceGw;
import com.vaiv.analyticsManager.apiGw.domain.TemplateGw;
import com.vaiv.analyticsManager.apiGw.service.SandboxGwService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/sandbox/*")
public class SandboxGwController {
	
	@Autowired
	private SandboxGwService sandboxGwService;
	
	@Autowired
	private RestFullReturnService restFullReturnService;
	
	
	/**
	 * 샌드박스 템플릿 신청 이력 조회 API
	 * @return
	 */
	@GetMapping(value="/customAnalysisTemplateRequests")
    public ResponseEntity<Object> customAnalysisTemplateRequestsGw(HttpSession session) {
        try {
        	return new ResponseEntity<Object>(sandboxGwService.customAnalysisTemplateRequestsGw(session),HttpStatus.OK);
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("customAnalysisTemplateRequestsGw",e);
		}
    }
	
	/**
	 * 샌드박스 템플릿 신청이력 개별조회 API
	 * @param id
	 * @return
	 */
	@GetMapping(value="/customAnalysisTemplateRequests/{customTemplateId}")
	public ResponseEntity<Object> customAnalysisTemplateRequestGw(@PathVariable String customTemplateId){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.customAnalysisTemplateRequestGw(customTemplateId);
			return restFullReturnService.restReturn(result, null);
		
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("customAnalysisTemplateRequestGw",e);
		}
	}
	
	/**
	 * 샌드박스 템플릿 추가 요청
	 * @param templateGw
	 * @param session
	 * @return
	 */
	@PostMapping(value="/customAnalysisTemplateRequests")
	public ResponseEntity<Object> customAnalysisTemplateRequestsAsPostGw(@RequestBody TemplateGw templateGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = sandboxGwService.customAnalysisTemplateRequestsAsPostGw(templateGw, session);
        	return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("customAnalysisTemplateRequestsAsPostGw",e);
		}
	}
	
	/**
	 * 샌드박스 템플릿  생성요청 취소 또는 커스텀 샌드박스 관리자 승인 또는 거절 또는 완료
	 * state => standby:대기 / reject:거절 / ongoing:생성준비중 / done:생성완료 / cancel:취소
	 * @param templateId
	 * @param state
	 * @return
	 */
	@PatchMapping(value="/customAnalysisTemplateRequests/{customTemplateId}")
	public ResponseEntity<Object> customAnalysisTemplateRequestsAsPatchGw(@PathVariable String customTemplateId, @RequestBody TemplateGw templateGw,
			HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.customAnalysisTemplateRequestsAsPatchGw(customTemplateId, templateGw, session);
			return restFullReturnService.restReturn(result, "ok");
            	
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("customAnalysisTemplateRequestsAsPatchGw",e);
		}
	}
	
	
	
	
	
	
	/**
	 * 샌드박스 템플릿 조회 API
	 * @return
	 */
	@GetMapping(value="/analysisTemplates")
    public ResponseEntity<Object> analysisTemplatesGw(HttpSession session) {
        try {
        	return new ResponseEntity<Object>(sandboxGwService.analysisTemplatesGw(session),HttpStatus.OK);
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("analysisTemplatesGw",e);
		}
    }
	
	/**
	 * 샌드박스 템플릿 상세조회 API
	 * @param id
	 * @return
	 */
	@GetMapping(value="/analysisTemplates/{templateId}")
	public ResponseEntity<Object> analysisTemplate(@PathVariable String templateId){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.analysisTemplateGw(templateId);
			return restFullReturnService.restReturn(result, null);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("analysisTemplateGw",e);
		}
	}
	
	
	/**
	 * 템플릿 생성
	 * @param templateGw
	 * @return
	 */
	@PostMapping(value="/analysisTemplates")
	public ResponseEntity<Object> analysisTemplatesAsPostGw(@RequestBody TemplateGw templateGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = sandboxGwService.analysisTemplatesAsPostGw(templateGw, session);
			return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("analysisTemplatesAsPostGw",e);
		}
	}
	
	
	/**
	 * 템플릿 수정
	 * @param template
	 * @return
	 */
	@PatchMapping(value="/analysisTemplates/{templateId}")
	public ResponseEntity<Object> analysisTemplatesAsPatchGw(@RequestBody TemplateGw templateGw, @PathVariable String templateId, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = sandboxGwService.analysisTemplatesAsPatchGw(templateId, templateGw, session);
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("analysisTemplatesAsPatchGw",e);
		}
	}
	
	
	/**
	 * 템플릿 삭제
	 * @param templateId
	 * @return
	 */
	@DeleteMapping(value="/analysisTemplates/{templateId}")
	public ResponseEntity<Object> analysisTemplatesAsDeleteGw(@PathVariable String templateId, HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.analysisTemplatesAsDeleteGw(templateId, session);
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("analysisTemplatesAsDeleteGw",e);
		}
	}
	
	
	
	
	
	

	
	/**
	 * 샌드박스 리스트 조회 API
	 * @return
	 */
	@GetMapping(value="/instances")
    public ResponseEntity<Object> instancesGw(HttpSession session) {
        try {
        	return new ResponseEntity<Object>(sandboxGwService.instancesGw(session),HttpStatus.OK);
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("instancesGw",e);
		}
    }
	
	/**
	 * 샌드박스 개별 조회 API
	 * @param id
	 * @return
	 */
	@GetMapping(value="/instances/{instanceId}")
	public ResponseEntity<Object> instanceGw(@PathVariable String instanceId){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.instanceGw(instanceId);
			return restFullReturnService.restReturn(result, null);
		
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instanceGw",e);
		}
	}
	
	/**
	 * 샌드박스 생성
	 * @param instance
	 * @param session
	 * @return
	 */
	@PostMapping(value="/instances")
	public ResponseEntity<Object> instancesAsPostGw(@RequestBody InstanceGw instanceGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = sandboxGwService.instancesAsPostGw(instanceGw, session);
			return restFullReturnService.restReturn(result, "create");
            	
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instancesAsPostGw",e);
		}
	}
	
	/**
	 * 샌드박스 시작/정지
	 * @param instanceId
	 * @return
	 */
	@PatchMapping(value="/instances/{instanceId}")
	public ResponseEntity<Object> instanceAsPatchGw(@PathVariable String instanceId, @RequestBody InstanceGw instanceGw){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.instanceAsPatchGw(instanceId, instanceGw.getServerState());
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instanceAsPatchGw",e);
		}
	}
	
	
	/**
	 * 샌드박스 삭제
	 * @param instanceId
	 * @return
	 */
	@DeleteMapping(value="/instances/{instanceId}")
	public ResponseEntity<Object> instanceAsDeleteGw(HttpSession session, @PathVariable String instanceId){
		JSONObject result = new JSONObject();
		try {
			result = sandboxGwService.instanceAsDeleteGw(session, instanceId);
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instanceAsDeleteGw",e);
		}
	}
	
	/**
	 * 샌드박스 로컬파일 조회
	 * @return
	 */
	@GetMapping(value="/instances/{selectedInstance}/localFiles")
	public ResponseEntity<Object> instancesLocalFilesGw(@PathVariable Integer selectedInstance){
		JSONObject result = new JSONObject();
		try {

			result = sandboxGwService.instancesLocalFilesGw(selectedInstance);
			return restFullReturnService.restReturn(result, null);
        	
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instancesLocalFilesGw",e);
		}
	}

	/**
	 * 샌드박스 로컬파일 샘플 조회
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 */
	@GetMapping(value="/instances/{selectedInstance}/localFiles/{localFile}")
	public ResponseEntity<Object> instancesLocalFileSampleGw(@PathVariable Integer selectedInstance, @PathVariable String localFile){
		JSONObject result = new JSONObject();
		try {

			result = sandboxGwService.instancesLocalFileSampleGw(selectedInstance, localFile);
			return restFullReturnService.restReturn(result, null);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("instancesLocalFileSampleGw",e);
		}
	}
}
