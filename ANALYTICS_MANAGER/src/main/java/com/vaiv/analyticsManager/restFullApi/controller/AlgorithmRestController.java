package com.vaiv.analyticsManager.restFullApi.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.service.AlgorithmRestService;

import net.sf.json.JSONObject;


@RestController
@RequestMapping("/UI/*")
public class AlgorithmRestController{
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AlgorithmRestService algorithmRestService;

	/**
	 * 알고리즘 조회
	 * @return
	 */
	@GetMapping(value="/algorithms")
    public ResponseEntity<JSONObject> algorithms() {
    	JSONObject result = new JSONObject();
        try {
        	result = algorithmRestService.algorithms();
        	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "algorithms");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
    }
	
	/**
	 * 알고림즘 상세조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/algorithms/{id}")
	public ResponseEntity<JSONObject> algorithm(@PathVariable Integer id){
		JSONObject result = new JSONObject();
        try {
        	if( MakeUtil.isNotNullAndEmpty(id) ) {
        		result = algorithmRestService.algorithm(id);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);	
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "algorithm");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 알고리즘 검색 조회
	 * @param value
	 * @return
	 */
	@PostMapping(value="/searchAlgorithms")
    public ResponseEntity<JSONObject> searchAlgorithms(@RequestBody Map<String, Object> params) {
    	JSONObject result = new JSONObject();
        try {
        	result = algorithmRestService.searchAlgorithms(""+params.get("searchValue"));
        	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "searchAlgorithms");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
    }
    
}