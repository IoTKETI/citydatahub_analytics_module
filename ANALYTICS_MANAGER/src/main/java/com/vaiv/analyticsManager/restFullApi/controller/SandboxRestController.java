package com.vaiv.analyticsManager.restFullApi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.service.SandboxRestService;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/UI/sandbox/*")
public class SandboxRestController {
	
	@Autowired
	private SandboxRestService sandboxRestService;

	@Value("${admin.nifiUrl}")
	private String nifiUrl;

	@Value("${admin.hueUrl}")
	private String hueUrl;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * 인스턴스 리스트 조회(프로잭트 등록에 사용)
	 * @return
	 */
	@GetMapping(value="/instances")
	public ResponseEntity<JSONObject> instances(HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.instances(session);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instances");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 도메인명 목록 가져오기
	 * @return
	 */
	@GetMapping(value="/availableList")
	public ResponseEntity<JSONObject> availableList(HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.allModelList();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "availableList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 로컬파일 조회
	 * @return
	 */
	@GetMapping(value="/instances/{selectedInstance}/localFiles")
	public ResponseEntity<JSONObject> instancesLocalFiles(@PathVariable Integer selectedInstance){
		JSONObject result = new JSONObject();
		try {

			if( MakeUtil.isNotNullAndEmpty(selectedInstance) ) {
				result = sandboxRestService.instancesLocalFiles(selectedInstance);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instancesLocalFiles");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 로컬파일 샘플 조회
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 */
	@GetMapping(value="/instances/{selectedInstance}/localFiles/{localFile}")
	public ResponseEntity<JSONObject> instancesLocalFileSample(@PathVariable Integer selectedInstance, @PathVariable String localFile){
		JSONObject result = new JSONObject();
		try {

			if( MakeUtil.isNotNullAndEmpty(selectedInstance) && MakeUtil.isNotNullAndEmpty(localFile) ) {
				result = sandboxRestService.instancesLocalFileSample(selectedInstance, localFile);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instancesLocalFileSample");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
     * session 에 Nifi/Hue Url 등록
     * @param instancePk
     * @param session
     * @return
     */
	@GetMapping(value="/getUrlInSession/{type}")
	public ResponseEntity<String> getUrlInSession(HttpSession session,  HttpServletRequest request
																,@PathVariable String type){

		logger.info("******** Controller /getUrlInSession");
		logger.info(request.toString());

		String url = "";
		if("Hue".equals(type)){
			url = hueUrl;
		}else if("Nifi".equals(type)){
			url = nifiUrl;
		}
		logger.info(url);
		logger.info(type);

		return new ResponseEntity<String> (url,HttpStatus.OK);
	}

}
