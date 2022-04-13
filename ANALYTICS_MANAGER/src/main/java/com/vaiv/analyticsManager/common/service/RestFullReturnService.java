package com.vaiv.analyticsManager.common.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.vaiv.analyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONObject;

@Component
public class RestFullReturnService {
	/**
	 * exception Failed
	 * @param errorMessage
	 * @param e
	 * @return
	 */
	public ResponseEntity<Object> exceptionFailed(String errorMessage, Exception e){
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/InternalError");
		result.put("title", "Internal Error");
		result.put("detail", e.toString());
		MakeUtil.printErrorLogger(e, "analysisTemplates");
		return new ResponseEntity<Object>(result,HttpStatus.EXPECTATION_FAILED);
	}
	
	/**
	 * Exception Bad Request
	 * @param detail
	 * @param errorMessage
	 * @return
	 */
	public ResponseEntity<Object> exceptionBadRequest(String detail, String errorMessage){
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/InvalidRequest");
		result.put("title", "Invalid Request");
		result.put("detail", detail);
		return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
	}
	
	
	/**
	 * rest ResponseEntity return
	 * @param result
	 * @param option
	 * @return
	 */
	public ResponseEntity<Object> restReturn(JSONObject result, String option){
		if( "Bad Request Data".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.BAD_REQUEST);
			
		}else if( "Resource Not Found".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.NOT_FOUND);
			
		}else if( "Already Exists".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.CONFLICT);
			
		}else if( "Unauthorized".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.UNAUTHORIZED);
			
		}else if( "Operation Not Supported".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.UNPROCESSABLE_ENTITY);
			
		}else if( "Internal Error".equals(result.get("title")) ){
			return new ResponseEntity<Object>(result,HttpStatus.EXPECTATION_FAILED);
			
		}else {
			if( "create".equals(option) ) {
				return new ResponseEntity<Object>(HttpStatus.CREATED);
				
			}else if( "ok".equals(option) ) {
				return new ResponseEntity<Object>(HttpStatus.OK);
				
			}else {
				return new ResponseEntity<Object>(result,HttpStatus.OK);
			}
			
		}
	}
	
	public JSONObject badRequestData(String detail) {
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/BadRequestData");
		result.put("title", "Bad Request Data");
		result.put("detail", detail);
		return result;
	}
	
	public JSONObject resourceNotFound(String detail) {
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/ResourceNotFound");
		result.put("title", "Resource Not Found");
		result.put("detail", detail);
		return result;
	}
	
	public JSONObject alreadyExists(String detail) {
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/AlreadyExists");
		result.put("title", "Already Exists");
		result.put("detail", detail);
		return result;
	}
	
	public JSONObject unauthorized(String detail) {
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/Unauthorized");
		result.put("title", "Unauthorized");
		result.put("detail", detail);
		return result;
	}
	
	public JSONObject internalError(Object detail) {
		JSONObject result = new JSONObject();
		result.put("type", "http://citydatahub.kr/errors/InternalError");
		result.put("title", "Internal Error");
		result.put("detail", detail);
		return result;
	}
	
	
}
