package com.vaiv.analyticsManager.apiGw.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vaiv.analyticsManager.apiGw.domain.BatchGw;
import com.vaiv.analyticsManager.apiGw.service.BatchGwService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;

import net.sf.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
public class BatchGwController {
	
	@Autowired
	private BatchGwService batchGwService;

	@Autowired
	private RestFullReturnService restFullReturnService;
	
	
	/**
	 * 배치 목록 조회 API
	 * @param session
	 * @return
	 */
	@GetMapping(value="/batchServices")
    public ResponseEntity<Object> batchServicesGw(HttpSession session) {
        try {
        	return new ResponseEntity<Object>(batchGwService.batchServicesGw(session),HttpStatus.OK);
        	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchServices",e);
		}
    }
	
	/**
	 * 배치 개별 조회 API
	 * @param batchServiceId
	 * @return
	 */
	@GetMapping(value="/batchServices/{batchServiceId}")
	public ResponseEntity<Object> batchServiceGw(HttpSession session, @PathVariable String batchServiceId){
		JSONObject result = new JSONObject();
        try {
				result = batchGwService.batchServiceGw(session, batchServiceId);
				return restFullReturnService.restReturn(result, null);
            	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchService",e);
		}
	}	
	
	
	
	/**
	 * 배치 등록
	 * @param batch
	 * @param session
	 * @return
	 */
	@PostMapping(value="/batchServices")
	public ResponseEntity<Object> batchServicesAsPostGw(@RequestBody BatchGw batchGw, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = batchGwService.batchServicesAsPostGw(session, batchGw);
			return restFullReturnService.restReturn(result, "create");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchServicesAsPost",e);
		}
	}
	
	/**
	 * 배치 수정
	 * @param batchServiceId
	 * @param batch
	 * @param session
	 * @return
	 */
	@PatchMapping(value="/batchServices/{batchServicePk}")
	public ResponseEntity<Object> batchServicesAsPatchGw(HttpSession session, @PathVariable String batchServicePk, @RequestBody BatchGw batchGw){
		JSONObject result = new JSONObject();
		
		try {
			result = batchGwService.batchServicesAsPatchGw(session, batchGw, batchServicePk);
        	return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchServicesAsPatch",e);
		}
	}
	
	/**
	 * 배치 삭제
	 * @param batchServiceId
	 * @return
	 */
	@DeleteMapping(value="/batchServices/{batchServicePk}")
	public ResponseEntity<Object> batchServicesAsDeleteGw(HttpSession session, @PathVariable String batchServicePk){
		JSONObject result = new JSONObject();
		try {
			result = batchGwService.batchServicesAsDeleteGw(session, batchServicePk);
			return restFullReturnService.restReturn(result, "ok");
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchServicesAsDelete",e);
		}
	}
	
	/**
	 * 배치서버 목록 조회
	 * @return
	 */
	@GetMapping(value="/batch/instances")
	public ResponseEntity<Object> batchServersGw(HttpSession session){
		try {
			return new ResponseEntity<Object>(batchGwService.batchServersGw(), HttpStatus.OK);
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchServers",e);
		}
	}
	
	/**
	 * 배치 서버 개별 조회
	 * @param batchServiceId
	 * @return
	 */
	@GetMapping(value="/batch/instances/{batchInstancePk}")
	public ResponseEntity<Object> batchServerGw(@PathVariable String batchInstancePk){
		JSONObject result = new JSONObject();
        try {
				result = batchGwService.batchServerGw(batchInstancePk);
				return restFullReturnService.restReturn(result, null);
            	
		} catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchService",e);
		}
	}	

	/**
	 * 배치 로그 조회
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchLogs")
	public ResponseEntity<Object> batchLogsGw(HttpSession session, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate){
		try {
			if(startDate==null|| endDate==null){
				LocalDate currentDate = LocalDate.now();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				endDate = currentDate.format(dateTimeFormatter);

				LocalDate threeMonthBeforeDate = LocalDate.now().minusMonths(3);
				startDate=threeMonthBeforeDate.format(dateTimeFormatter);
			}

        	return new ResponseEntity<Object>(batchGwService.batchLogsGw(session, startDate, endDate),HttpStatus.OK);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchLogsGw",e);
		}
	}
	
	/**
	 * 배치 개별 로그 조회
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchLogs/{logBatchSequencePk}")
	public ResponseEntity<Object> batchLogGw(HttpSession session, @PathVariable String logBatchSequencePk){
		try {
        	return new ResponseEntity<Object>(batchGwService.batchLogGw(session, logBatchSequencePk),HttpStatus.OK);
			
		}catch (Exception e) {
			return restFullReturnService.exceptionFailed("batchLogGw",e);
		}
	}
	

	
}
