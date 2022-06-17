package com.vaiv.analyticsManager.restFullApi.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Batch;
import com.vaiv.analyticsManager.restFullApi.domain.SearchData;
import com.vaiv.analyticsManager.restFullApi.service.BatchRestService;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/UI/*")
public class BatchRestController {
	
	@Autowired
	private BatchRestService batchRestService;

	@Value("${admin.nifiUrl}")
	private String adminNifiUrl;
	
	/**
	 * 배치 조회
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchServices/{batchServiceSequencePk}")
	public ResponseEntity<JSONObject> batchService(@PathVariable Integer batchServiceSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(batchServiceSequencePk) ) {
				result = batchRestService.batchService(batchServiceSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchService");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 배치 등록
	 * @param batch
	 * @param session
	 * @return
	 */
	@PostMapping(value="/batchServices")
	public ResponseEntity<JSONObject> batchServicesAsPost(@RequestBody Batch batch, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(batch) ) {
				batch.setUserId(""+session.getAttribute("userId"));
				result = batchRestService.batchServicesAsPost(batch);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServicesAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 수정
	 * @param batchServiceSequencePk
	 * @param batch
	 * @param session
	 * @return
	 */
	@PatchMapping(value="/batchServices/{batchServiceSequencePk}")
	public ResponseEntity<JSONObject> batchServicesAsPatch(@PathVariable Integer batchServiceSequencePk, @RequestBody Batch batch){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(batchServiceSequencePk) ) {
				result = batchRestService.batchServicesAsPatch(batch);
				batchRestService.batchServicesUpdate(batchServiceSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServicesAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 삭제
	 * @param batchServiceSequencePk
	 * @return
	 */
	@DeleteMapping(value="/batchServices/{batchServiceSequencePk}")
	public ResponseEntity<JSONObject> batchServicesAsDelete(@PathVariable Integer batchServiceSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(batchServiceSequencePk) ) {
				result = batchRestService.batchServicesAsDelete(batchServiceSequencePk);
				batchRestService.batchServicesUpdate(batchServiceSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServicesAsDelete");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치서버 목록 조회
	 * @return
	 */
	@GetMapping(value="/batchServers")
	public ResponseEntity<JSONObject> batchServers(){
		JSONObject result = new JSONObject();
		try {
			result = batchRestService.batchServers();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServers");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 Admin Nifi 주소 가져오기
	 * @param session
	 * @param session
	 * @return
	 */
	@GetMapping(value="/batchServices/adminNifiUrl")
	public ResponseEntity<JSONObject> batchServicesAdminNifiUrl(HttpSession session){
		JSONObject result = new JSONObject();

		try {
			if(!"Analytics_Admin".equals(session.getAttribute("userRole").toString()) ) {
				result.put("type", "5000");
				result.put("detail", "Unauthorized");
				return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
			}
			result.put("nifiUrl",adminNifiUrl);
			return new ResponseEntity<JSONObject>(result,HttpStatus.OK);

		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServicesAdminNifiUrl");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 배치 시작/정지
	 * @param batch
	 * @param batch
	 * @return
	 */
	@PatchMapping(value="/batchServices/startAndStop")
	public ResponseEntity<JSONObject> batchServicesStartAndStop(@RequestBody Batch batch){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(batch) ) {
				result = batchRestService.batchServicesStartAndStop(batch);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchServicesStartAndStop");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 로그 조회
	 * @param batchServiceSequencePk
	 * @return
	 */
	@RequestMapping(value="/batchLogs", method={RequestMethod.POST, RequestMethod.GET})
	public String batchLogs(@ModelAttribute SearchData searchData, HttpSession session){
		String result = null;
		try {
			if( MakeUtil.isNotNullAndEmpty(searchData) ) {
				String userRole = ""+session.getAttribute("userRole");
				String userId = ""+session.getAttribute("userId");
				if( "Analytics_Admin".equals(userRole) ) userId = "";
				
				searchData.setUserId(userId);
				result = batchRestService.batchLogs(searchData);
        	}
			
		}catch (Exception e) {
			MakeUtil.printErrorLogger(e, "batchService");
			return result;
		}
		return result;
	}
	
	/**
	 * 배치 개별 로그 조회
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchLog/{logBatchSequencePk}")
	public ResponseEntity<JSONObject> batchLog(@PathVariable Integer logBatchSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(logBatchSequencePk) ) {
				result = batchRestService.batchLog(logBatchSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "batchService");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
}
