package com.vaiv.analyticsManager.apiGw.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	 * 인스턴스 리스트 조회 API
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
	 * 인스턴스 개별 조회 API
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
	 * 인스턴스 로컬파일 조회
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
	 * 인스턴스 로컬파일 샘플 조회
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
