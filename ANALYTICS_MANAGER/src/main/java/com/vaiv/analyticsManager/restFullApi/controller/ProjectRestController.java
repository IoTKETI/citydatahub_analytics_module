package com.vaiv.analyticsManager.restFullApi.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Model;
import com.vaiv.analyticsManager.restFullApi.domain.OriginalData;
import com.vaiv.analyticsManager.restFullApi.domain.Project;
import com.vaiv.analyticsManager.restFullApi.service.ProjectRestService;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/UI/*")
public class ProjectRestController {

	@Autowired
	private ProjectRestService projectRestService;
	
	/**
	 * 프로젝트 리스트 조회
	 * @return
	 */
	@GetMapping(value="/projects")
	public ResponseEntity<JSONObject> projects(HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = projectRestService.projects(session);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projects");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 프로젝트 개별 조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<JSONObject> project(@PathVariable Integer projectSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) ) {
				result = projectRestService.project(projectSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "project");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 프로젝트 등록
	 * @param project
	 * @return
	 */
	@PostMapping(value="/projects")
	public ResponseEntity<JSONObject> projectsAsPost(@RequestBody Project project, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(project) ) {
				project.setUserId(""+session.getAttribute("userId"));
				result = projectRestService.projectsAsPost(project);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectsAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 프로젝트 수정
	 * @param project
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<JSONObject> projectsAsPatch(@PathVariable Integer projectSequencePk, @RequestBody Project project, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) ) {
				project.setUserId(""+session.getAttribute("userId"));
				result = projectRestService.projectsAsPatch(projectSequencePk, project);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectsAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 프로젝트 삭제
	 * @param projectSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}")
	public ResponseEntity<JSONObject> projectAsDelete(@PathVariable Integer projectSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) ) {
				result = projectRestService.projectAsDelete(projectSequencePk, null);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectAsDelete");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 원본 데이터 조회
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/originalData")
	public ResponseEntity<JSONObject> originalDataList(@PathVariable Integer projectSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) ) {
				result = projectRestService.originalDataList(projectSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
				
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 원본 데이터 개별 조회
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject> originalData(@PathVariable Integer projectSequencePk, @PathVariable Integer originalDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk) ) {
				
				result = projectRestService.originalData(projectSequencePk, originalDataSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalData");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 원본 데이터 생성
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PostMapping(value="/projects/{projectSequencePk}/originalData")
	public ResponseEntity<JSONObject> originalDataAsPost(@PathVariable Integer projectSequencePk, @RequestBody OriginalData originalData){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalData) ) {
				
				result = projectRestService.originalDataAsPost(originalData);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 원본 데이터 전처리 테스트
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject>originalDataAsPatch(@PathVariable Integer projectSequencePk
													   , @PathVariable Integer originalDataSequencePk
													   , @RequestBody Map<String, Object> requestTtest){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk) 
					&& MakeUtil.isNotNullAndEmpty(requestTtest)) {
				result = projectRestService.originalDataAsPatch(projectSequencePk, originalDataSequencePk, requestTtest);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 원본 데이터 삭제
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject> originalDataAsDelete(@PathVariable Integer projectSequencePk, @PathVariable Integer originalDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk) ) {
				result = projectRestService.originalDataAsDelete(projectSequencePk, originalDataSequencePk, null);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsDelete");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 전처리 처리방식 목록 가져오기
	 * @return
	 */
	@GetMapping(value="/preprocessFunctions")
	public ResponseEntity<JSONObject> preprocessFunctionList(){
		JSONObject result = new JSONObject();
		try {
			result = projectRestService.preprocessFunctionList();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessFunctionList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 전처리 처리방식 가져오기
	 * @param preprocessFunctionSequencePk
	 * @return
	 */
	@GetMapping(value="/preprocessFunctions/{preprocessFunctionSequencePk}")
	public ResponseEntity<JSONObject> preprocessFunction(@PathVariable Integer preprocessFunctionSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(preprocessFunctionSequencePk) ) {
				result = projectRestService.preprocessFunction(preprocessFunctionSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
				
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessFunction");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
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
	public ResponseEntity<JSONObject>preprocessedDataAsPost(@PathVariable Integer projectSequencePk, @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(params)) {
				result = projectRestService.preprocessedDataAsPost(projectSequencePk, params);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedDataAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 전처리 데이터 목록 가져오기
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/{instancePk}/originalData/{originalDataSequencePk}/preprocessedData")
	public ResponseEntity<JSONObject> preprocessedDataList(@PathVariable Integer instancePk, @PathVariable Integer originalDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(instancePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk) ) {
				result = projectRestService.preprocessedDataList(instancePk, originalDataSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
				
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedDataList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 전처리 데이터 개별 조회
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value="/{instancePk}/originalData/{originalDataSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<JSONObject> preprocessedData(@PathVariable Integer instancePk, @PathVariable Integer originalDataSequencePk, @PathVariable Integer preprocessedDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(instancePk) 
					&& MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk)
					&& MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk) ) {
				
				result = projectRestService.preprocessedData(instancePk, originalDataSequencePk, preprocessedDataSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedData");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 전처리 데이터 삭제
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<JSONObject> preprocessedDataAsDelete(@PathVariable Integer projectSequencePk, @PathVariable Integer preprocessedDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk) ) {
				result = projectRestService.preprocessedDataAsDelete(projectSequencePk, preprocessedDataSequencePk, null);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsDelete");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 모델 생성
	 * @param projectSequencePk
	 * @param params
	 * @return
	 */
	@PostMapping(value="/projects/{projectSequencePk}/models")
	public ResponseEntity<JSONObject>modelsAsPost(@PathVariable Integer projectSequencePk, @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(params)) {
				result = projectRestService.modelsAsPost(projectSequencePk, params);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 모델 목록 조회
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/models")
	public ResponseEntity<JSONObject> modelsList(@PathVariable Integer projectSequencePk
			, @RequestParam(value="preprocessedDataSequencePk", required = false) Integer preprocessedDataSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) ) {
				Model model = new Model();
				model.setProjectSequenceFk4(projectSequencePk);
				if( MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk) )	model.setPreprocessedDataSequenceFk2(preprocessedDataSequencePk);
				result = projectRestService.modelsList(model);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
				
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 모델 개별 조회
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@GetMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> model(@PathVariable Integer projectSequencePk, @PathVariable Integer modelSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk) ) {
				
				result = projectRestService.model(projectSequencePk, modelSequencePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "model");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 모델 삭제
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@DeleteMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> modelsAsDelete(@PathVariable Integer projectSequencePk, @PathVariable Integer modelSequencePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk) ) {
				result = projectRestService.modelsAsDelete(projectSequencePk, modelSequencePk, null);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsDelete");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 학습 중지, 모델재생성 을 포함한 모델 수정
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@PatchMapping(value="/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> modelsAsPatch(@PathVariable Integer projectSequencePk
				, @PathVariable Integer modelSequencePk,  @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk) ) {
				result = projectRestService.modelsAsPatch(projectSequencePk, modelSequencePk, params);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
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
	public ResponseEntity<JSONObject> modelsTestAsPatch(@PathVariable Integer projectSequencePk
									, @PathVariable Integer modelSequencePk,  @RequestBody Map<String, Object> params){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk) ) {
				result = projectRestService.modelsTestAsPatch(projectSequencePk, modelSequencePk, params);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsTestAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 인스턴스별 모델 목록 가져오기
	 * @param session
	 * @return
	 */
	@GetMapping(value="/modelsOfInstancePk/{instanceSequencePk}")
	public ResponseEntity<JSONObject> modelsOfInstancePk(@PathVariable Integer instanceSequencePk){
		JSONObject result = new JSONObject();
		try {
			result = projectRestService.modelsOfInstancePk(instanceSequencePk);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsOfInstancePk");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
}
	