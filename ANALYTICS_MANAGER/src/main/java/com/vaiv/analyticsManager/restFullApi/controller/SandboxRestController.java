package com.vaiv.analyticsManager.restFullApi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Instance;
import com.vaiv.analyticsManager.restFullApi.domain.Template;
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
	 * 샌드박스 리스트 조회
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
	 * 샌드박스 개별 조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/instances/{instancePk}")
	public ResponseEntity<JSONObject> instance(@PathVariable Integer instancePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(instancePk) ) {
				result = sandboxRestService.instance(instancePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instance");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 샌드박스 사양 조회
	 * @return
	 */
	@GetMapping(value="/specifications")
	public ResponseEntity<JSONObject> specifications(){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.specifications();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "specifications");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 사양 상세조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/specifications/{serverId}")
	public ResponseEntity<JSONObject> specification(@PathVariable String serverId){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(serverId) ) {
				result = sandboxRestService.specification(serverId);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);	
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "specification");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 템플릿 조회
	 * @return
	 */
	@GetMapping(value="/templates")
	public ResponseEntity<JSONObject> templates(HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			result = sandboxRestService.templates(session);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "templates");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 템플릿 상세조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/templates/{templateId}")
	public ResponseEntity<JSONObject> template(@PathVariable Integer templateId){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(templateId) ) {
				result = sandboxRestService.template(templateId);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);	
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "template");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 템플릿 신청 이력 조회
	 * @return
	 */
	@GetMapping(value="/customTemplateRequests")
	public ResponseEntity<JSONObject> customTemplateRequests(HttpSession session){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.customTemplateRequests(session);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "customTemplateRequests");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 템플릿 신청이력 개별조회
	 * @param id
	 * @return
	 */
	@GetMapping(value="/customTemplateRequests/{templateId}")
	public ResponseEntity<JSONObject> customTemplateRequest(@PathVariable Integer templateId){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(templateId) ) {
				result = sandboxRestService.customTemplateRequest(templateId);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "customTemplateRequest");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 템플릿 허용 목록 가져오기
	 * @return
	 */
	@GetMapping(value="/availableList")
	public ResponseEntity<JSONObject> availableList(HttpSession session){
		JSONObject result = new JSONObject();
		try {
			String userId = ""+session.getAttribute("userId");
			String userRole = ""+session.getAttribute("userRole");

			if(userRole.equals("Analytics_Admin")){
				result = sandboxRestService.allModelList();
				return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
			}

			result = sandboxRestService.availableList(userId);
        	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "availableList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 스냅샷 목록 가져오기
	 * @return
	 */
	@GetMapping(value="/snapshotList")
	public ResponseEntity<JSONObject> snapshotList(){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.snapshotList();
        	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "availableList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 템플릿 허용 데이터 가져오기
	 * @param id
	 * @return
	 */
	@GetMapping(value="/availableDataList/{id}")
	public ResponseEntity<JSONObject> availableDataList(@PathVariable String id){
		JSONObject result = new JSONObject();
		try {
			result = sandboxRestService.availableDataList(id);
        	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "availableDataList");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 샌드박스 템플릿 추가 요청
	 * @param template
	 * @param session
	 * @return
	 */
	@PostMapping(value="/customTemplateRequests")
	public ResponseEntity<JSONObject> customTemplateRequestsAsPost(@RequestBody Template template, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(template) ) {
				template.setUserId(""+session.getAttribute("userId"));
				sandboxRestService.customTemplateRequestsAsPost(template);
				result.put("result", "success");
				result.put("type", "2004");
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "customTemplateRequestsAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 템플릿  생성요청 취소 또는 커스텀 샌드박스 관리자 승인 또는 거절 또는 완료
	 * state => standby:대기 / reject:거절 / ongoing:생성준비중 / done:생성완료 / cancel:취소
	 * @param templateId
	 * @param state
	 * @return
	 */
	@PatchMapping(value="/customTemplateRequests/{templateId}")
	public ResponseEntity<JSONObject> customTemplateRequestsAsPatch(@PathVariable Integer templateId, @RequestBody Template template){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(templateId) ) {
				template.setTemplateId(templateId);
				sandboxRestService.customTemplateRequestsAsPatch(template);
				result.put("result", "success");
				result.put("type", "2004");
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "customTemplateRequestsAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 템플릿 생성
	 * @param template
	 * @return
	 */
	@PostMapping(value="/templates")
	public ResponseEntity<JSONObject> templatesAsPost(@RequestBody Template template){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(template) ) {
				result = sandboxRestService.templatesAsPost(template);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "templatesAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 템플릿 삭제
	 * @param templateId
	 * @return
	 */
	@DeleteMapping(value="/templates/{templateId}")
	public ResponseEntity<JSONObject> templateAsDelete(@PathVariable Integer templateId){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(templateId) ) {
				sandboxRestService.templateAsDelete(templateId);
				result.put("result", "success");
				result.put("type", "2001");
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);	
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "templateAsDelete templateId : "+templateId);
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 템플릿 수정
	 * @param template
	 * @return
	 */
	@PatchMapping(value="/templates/{templateId}")
	public ResponseEntity<JSONObject> templatesAsPatch(@RequestBody Template template,@PathVariable Integer templateId){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(template) ) {
				template.setTemplateId(templateId);
				result = sandboxRestService.templatesAsPatch(template);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "templatesAsPatch");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 샌드박스 생성
	 * @param instance
	 * @param session
	 * @return
	 */
	@PostMapping(value="/instances")
	public ResponseEntity<JSONObject> instancesAsPost(@RequestBody Instance instance, HttpSession session){
		JSONObject result = new JSONObject();
		
		try {
			if( MakeUtil.isNotNullAndEmpty(instance) ) {
				instance.setUserId(""+session.getAttribute("userId"));
				result = sandboxRestService.instancesAsPost(instance);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
            	
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instancesAsPost");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * 샌드박스 시작/정지
	 * @param instancePk
	 * @return
	 */
	@PatchMapping(value="/instances/{instancePk}")
	public ResponseEntity<JSONObject> instanceAsPatch(@PathVariable Integer instancePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(instancePk) ) {
				result = sandboxRestService.instanceAsPatch(instancePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instance");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	
	/**
	 * 샌드박스 삭제
	 * @param instancePk
	 * @return
	 */
	@DeleteMapping(value="/instances/{instancePk}")
	public ResponseEntity<JSONObject> instanceAsDelete(@PathVariable Integer instancePk){
		JSONObject result = new JSONObject();
		try {
			if( MakeUtil.isNotNullAndEmpty(instancePk) ) {
				result = sandboxRestService.instanceAsDelete(instancePk);
            	return new ResponseEntity<JSONObject>(result,HttpStatus.OK);
        	}else {
        		result.put("type", "4101");
    			result.put("detail", "MANDATORY PARAMETER MISSING");
        		return new ResponseEntity<JSONObject>(result,HttpStatus.BAD_REQUEST);
        	}
			
		}catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instance");
			return new ResponseEntity<JSONObject>(result,HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 샌드박스 로컬파일 조회
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
	 * 샌드박스 로컬파일 샘플 조회
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
     * session 에 instancePk등록
     * @param instancePk
     * @param session
     * @return
     */
	@GetMapping(value="/sandboxSetInstancePkInSession/{instancePk}")
	public ResponseEntity<String> sandboxSetInstancePkInSession(HttpSession session,  HttpServletRequest request
																,@PathVariable Integer instancePk){

		logger.info("******** Controller /sandboxSetInstancePkInSession/instancePk");
		logger.info(request.toString());

		String url = "http://"+request.getServerName()+":"+request.getServerPort();
		logger.info(url);
		logger.info(Integer.toString(instancePk));

		session.setAttribute("instancePk", String.valueOf(instancePk));
		return new ResponseEntity<String> (url,HttpStatus.OK);
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
