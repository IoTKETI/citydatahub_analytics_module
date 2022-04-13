package com.vaiv.analyticsManager.apiGw.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.apiGw.service.AlgorithmGwService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;

import net.sf.json.JSONObject;


@RestController
public class AlgorithmGwController{
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AlgorithmGwService algorithmGwService;
	
	@Autowired
	private RestFullReturnService restFullReturnService;

	/**
	 * 알고리즘 목록 조회 API
	 * @return
	 */
	@GetMapping(value="/algorithms")
    public ResponseEntity<Object> algorithmsGw() {
        try {
        	return new ResponseEntity<Object>(algorithmGwService.algorithmsGw(),HttpStatus.OK);
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("algorithmsGw",e);
		}
    }
	
	/**
	 * 알고림즘 상세조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/algorithms/{algorithmPk}")
	public ResponseEntity<Object> algorithm(@PathVariable String algorithmPk){
		JSONObject result = new JSONObject();
        try {
    		result = algorithmGwService.algorithmGw(algorithmPk);
    		return restFullReturnService.restReturn(result, null);
        		
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("algorithmGw",e);
		}
	}
	

    
}