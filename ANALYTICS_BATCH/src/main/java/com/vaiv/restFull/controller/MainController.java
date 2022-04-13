package com.vaiv.restFull.controller;

import com.vaiv.common.utils.MakeUtil;
import com.vaiv.restFull.service.BatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
public class MainController {
	
	@Autowired
	private BatchService batchService;

	/**
	 * 배치 전체 리스트
	 * @param sandboxInstanceSequence
	 * @return
	 */
	@GetMapping(value="/list")
	public ResponseEntity<JSONObject> batchListAllEnrolled(){
		JSONObject result = new JSONObject();
		try {
			result = batchService.list();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "list");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 배치 다시 시작
	 * @param sandboxInstanceSequence
	 * @return
	 */
	@GetMapping(value="/batch/{sandboxInstanceSequence}/refrash")
	public ResponseEntity<JSONObject> refrash(@PathVariable Long sandboxInstanceSequence){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(sandboxInstanceSequence) ) {
				result = batchService.refrash(sandboxInstanceSequence);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "refrash");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 모두 중지
	 * @param sandboxInstanceSequence
	 * @return
	 */
	@GetMapping(value="/batch/{sandboxInstanceSequence}/allStop")
	public ResponseEntity<JSONObject> allStop(@PathVariable Long sandboxInstanceSequence){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(sandboxInstanceSequence) ) {
				result = batchService.allStop(sandboxInstanceSequence);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "allStop");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 개별 시작
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchStart/{batchServiceSequencePk}")
	public ResponseEntity<JSONObject> batchStart(@PathVariable Long batchServiceSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(batchServiceSequencePk) ) {
                batchService.batchStop(batchServiceSequencePk);
				result = batchService.batchStart(batchServiceSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "start");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 배치 개별 중지
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	@GetMapping(value="/batchStop/{batchServiceSequencePk}")
	public ResponseEntity<JSONObject> batchStop(@PathVariable Long batchServiceSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(batchServiceSequencePk) ) {
				result = batchService.batchStop(batchServiceSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "stop");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

}
