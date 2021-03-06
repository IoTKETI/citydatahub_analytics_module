package com.vaiv.analyticsManager.apiGw.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.apiGw.domain.BatchGw;
import com.vaiv.analyticsManager.apiGw.mapper.BatchGwMapper;
import com.vaiv.analyticsManager.apiGw.mapper.ProjectGwMapper;
import com.vaiv.analyticsManager.apiGw.mapper.SandboxGwMapper;
import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;
import com.vaiv.analyticsManager.common.utils.FileUtil;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.common.utils.ZipUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class BatchGwService {

	private static Logger logger = LoggerFactory.getLogger(SandboxGwService.class);
	
	@Autowired
	private BatchGwMapper batchGwMapper;
	
	@Autowired
	private ProjectGwMapper projectGwMapper;
	
	@Autowired
	private SandboxGwService sandboxGwService;
	
	@Autowired
	private SandboxGwMapper sandboxGwMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private RestFullReturnService restFullReturnService;


	// Cloud API
	@Value("${cloudApi.url}")
	private String cloudApiUrl;

	@Value("${nfs.path}")
	private String nfsPath;
	
	@Value("${nfs.resultPath}")
	private String nfsResultPath;
	
	@Value("${analyticsBatchServer.url}")
	private String analyticsBatchServerUrl;
	
	
	/**
	 * 배치신청 목록 조회 API
	 * @param session
	 * @return
	 */
	public JSONArray batchServiceRequestsGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = batchGwMapper.batchServiceRequestsGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}

	/**
	 * 배치신청 조회 API
	 * @param batchServiceRequestSequencePk
	 * @return
	 */
	public JSONObject batchServiceRequestGw(HttpSession session, String batchServiceRequestSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		int batchServiceRequestSequenceId;
		try {
			batchServiceRequestSequenceId = Integer.parseInt(batchServiceRequestSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}


		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		Map<String, Object> detail = batchGwMapper.batchServiceRequestGw(batchServiceRequestSequenceId, userId);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found batchServiceRequest");
		}
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}

	
	/**
	 * 배치신청 등록
	 * @param batch
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServiceRequestsAsPostGw(HttpSession session, BatchGw batchGw) throws Exception {
		JSONObject result = new JSONObject();
		// 배치 명 중복 체크
		if( batchGwMapper.checkBatchNameGw(batchGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
		}

		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(batchGw.getProjectId());
		if( project == null ) {
			return restFullReturnService.resourceNotFound("Not found project");
		}

		/*모델 체크*/
		Map<String, Object> model = projectGwMapper.modelGw(batchGw.getProjectId(), batchGw.getModelId());
		if( model == null ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}
		
		// 배치신청 등록
		batchGw.setUserId(""+session.getAttribute("userId"));
		batchGwMapper.insertBatchServiceRequestGw(batchGw);
		return result;
	}

	
	/**
	 * 배치 목록 조회 API
	 * @param session
	 * @return
	 */
	public JSONArray batchServicesGw(HttpSession session) throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = batchGwMapper.batchServicesGw(userId);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}

	/**
	 * 배치 개별 조회 API
	 * @param batchServiceSequencePk
	 * @return
	 */
	public JSONObject batchServiceGw(HttpSession session, String batchServiceSequencePk) throws Exception {
		JSONObject result = new JSONObject();
		int batchServiceSequenceId;
		try {
			batchServiceSequenceId = Integer.parseInt(batchServiceSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}

		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		Map<String, Object> detail = batchGwMapper.batchServiceGw(batchServiceSequenceId, userId);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found batchService");
		}
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}

	
	/**
	 * 배치 등록
	 * @param batch
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public JSONObject batchServicesAsPostGw(HttpSession session, BatchGw batchGw) throws NumberFormatException, Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		// 배치 명 중복 체크
		if( batchGwMapper.checkBatchNameGw(batchGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
		}

		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(batchGw.getProjectId());
		if( project == null ) {
			return restFullReturnService.resourceNotFound("Not found project");
		}

		/*배치 인스턴스 체크*/
		Map<String, Object> batchInstance = sandboxGwMapper.instanceGw(batchGw.getSandboxInstanceId());
		if( batchInstance == null ) {
			return restFullReturnService.resourceNotFound("Not found batch instance");
		}

		/*모델 체크*/
		Map<String, Object> model = projectGwMapper.modelGw(batchGw.getProjectId(), batchGw.getModelId());
		if( model == null ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}

		/* 사용자 권한체크 */
		String userId = ""+session.getAttribute("userId");
		String userRole = ""+session.getAttribute("userRole");
		if( !"Analytics_Admin".equals(userRole)  ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
			
		/* 저장될 파일 위치 생성  ex)/data/parkingarea/parking{yyyyMMddHH}.json */
		String applyDataPath = batchGw.getApplyDataPath();
		if( !"/".equals(applyDataPath.substring(0,1)) )
			applyDataPath = "/"+applyDataPath;
		FileUtil.mkdir(nfsResultPath + applyDataPath);
		
		
		/****************************** 학습 모듈에 배치 전송 ************************************/
		// 인스턴스 내부IP 가져오기
		String ip = sandboxGwService.getInstanceIp(batchGw.getSandboxInstanceId());
		
		String listUrl = ip + "/batchInfo?model_id="+batchGw.getModelId();
		httpJson = httpService.httpServiceGET(listUrl, "");
		
		if( "200".equals(httpJson.get("type")) ) {
			
			// 배치 등록
			batchGw.setUserId(userId);
			if( batchGw.getManagerId() == null ) batchGw.setManagerId(batchGw.getUserId());
			batchGwMapper.insertBatchServicesGw(batchGw);
			
			
			/*************** 배치분석모듈에 BATCH_INFO 전송 *********************/
			String batchIp = sandboxGwService.getInstanceIp(batchGw.getBatchInstanceId());
			
			listUrl = batchIp+"/batchService";
			JSONObject batchInfoParam = JSONObject.fromObject(httpJson.get("data"));
			batchInfoParam.put("BATCH_SERVICE_SEQUENCE_PK", batchGw.getBatchServiceId());
			JSONObject batchHttpJson = httpService.httpServicePOST(listUrl, batchInfoParam.toString(), null);
			if( "201".equals(batchHttpJson.get("type")) ) {
				logger.info("send BatchModuleServer BATCH_INFO complete...");
			}else {
				logger.error("Error send BatchModuleServer BATCH_INFO..."+batchHttpJson.toString());
				throw new RuntimeException("Error send BatchModuleServer BATCH_INFO");
			}
			
			
			/*************** 전처리파일, 학습된 모델파일 API로 가져와서 NFS에 폴더 생성후 저장 *********************/
			// 전처리
			listUrl = ip + "/preprocessedData/"+batchInfoParam.get("PREPROCESSED_DATA_SEQUENCE_FK2")+"/download";
			String filePath = nfsPath+batchGw.getBatchServiceId();
			String fileName = "preprocessedData_"+batchGw.getBatchServiceId()+".zip";
			httpService.httpServiceDownloader(listUrl, filePath, fileName);
			// zip파일 압축 풀고 zip파일 삭제
			unzipAndDeleteGw(filePath, fileName);
			
			// 모델
			listUrl = ip + "/models/"+batchInfoParam.get("MODEL_SEQUENCE_FK1")+"/download";
			filePath = nfsPath+batchGw.getBatchServiceId();
			fileName = "models_"+batchGw.getBatchServiceId()+".zip";
			httpService.httpServiceDownloader(listUrl, filePath, fileName);
			// zip파일 압축 풀고 zip파일 삭제
			unzipAndDeleteGw(filePath, fileName);
			logger.info("preprocessedData, model Files save complete...");
			
			Map<String, Object> detail = batchGwMapper.batchServiceGw(batchGw.getBatchServiceId(), null);
			if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
			
			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			
		}else {
			resultJson.put("result", "error");
			resultJson.put("type", "5000");
		}
		return resultJson;
	}

	/**
	 * 배치 수정
	 * @param batch
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServicesAsPatchGw(HttpSession session, BatchGw batchGw, String batchServicePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		int batchServiceId;
		try {
			batchServiceId = Integer.parseInt(batchServicePk);
			batchGw.setBatchServiceId(batchServiceId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		// 배치 명 중복 체크
		if( batchGwMapper.checkBatchNameGw(batchGw) > 0 ) {
			return restFullReturnService.alreadyExists("duplicate name");
		}

		/* 프로젝트 체크 */
		Map<String, Object> project = projectGwMapper.projectGw(batchGw.getProjectId());
		if( project == null ) {
			return restFullReturnService.resourceNotFound("Not found project");
		}
		
		/*배치 인스턴스 체크*/
		Map<String, Object> batchInstance = sandboxGwMapper.instanceGw(batchGw.getSandboxInstanceId());
		if( batchInstance == null ) {
			return restFullReturnService.resourceNotFound("Not found batch instance");
		}

		/*모델 체크*/
		Map<String, Object> model = projectGwMapper.modelGw(batchGw.getProjectId(), batchGw.getModelId());
		if( model == null ) {
			return restFullReturnService.resourceNotFound("Not found model");
		}
		
		/* 배치 체크 */
		Map<String, Object> batchDetail = batchGwMapper.batchServiceGw(batchServiceId, null);
		if( batchDetail == null ) {
			return restFullReturnService.resourceNotFound("Not found batchService");
		}
		
		/* 사용자 권한체크 */
		String userRole = ""+session.getAttribute("userRole");
		if( !"Analytics_Admin".equals(userRole) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		// 배치 수정
		batchGwMapper.updateBatchServiceGw(batchGw);
		Map<String, Object> detail = batchGwMapper.batchServiceGw(batchGw.getBatchServiceId(), null);
		if( MakeUtil.isNotNullAndEmpty(detail) ) {
			resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		}else {
			return restFullReturnService.resourceNotFound("Not found batchService");
		}
		
		/*************** 배치서버에 전송 *********************/
		if( "start".equals(detail.get("lastBatchState")) ) {
			String listUrl = analyticsBatchServerUrl+"/batchStart/"+batchGw.getBatchServiceId();
			if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
				JSONObject batchServerHttpJson = httpService.httpServiceGET(listUrl, "");
				if( "200".equals(batchServerHttpJson.get("type")) ) {
					resultJson.put("result", "success");
					resultJson.put("type", "2001");
					logger.info("batchServer send "+batchGw.getBatchState()+" complete...");
				}else {
					resultJson.put("result", "fail");
					resultJson.put("type", "5000");
					resultJson.put("detail", batchServerHttpJson.get("data"));
					logger.error("Error batchServer send "+batchGw.getBatchState()+" error...");
				}
			}
		}
		return resultJson;
	}

	/**
	 * 배치 삭제
	 * @param batchServiceId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServicesAsDeleteGw(HttpSession session, String batchServicePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		int batchServiceId;
		try {
			batchServiceId = Integer.parseInt(batchServicePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		/* 사용자 권한체크 */
		String userRole = ""+session.getAttribute("userRole");
		if( !"Analytics_Admin".equals(userRole) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		Map<String, Object> detail = batchGwMapper.batchServiceGw(batchServiceId, null);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found batchService");
		}
		
		/*************** 배치서버에 전송 *********************/
		if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
			String listUrl = analyticsBatchServerUrl+"/batchStop/"+batchServiceId;
			JSONObject batchServerHttpJson = httpService.httpServiceGET(listUrl, "");
			if( "200".equals(batchServerHttpJson.get("type")) ) {
				// 배치 삭제
				BatchGw batchGw = new BatchGw();
				batchGw.setBatchServiceId(batchServiceId);
				batchGw.setDeleteFlag(true);
				batchGwMapper.updateBatchServiceGw(batchGw);
				logger.info("batchServer send complete...");
			}else {
				throw new RuntimeException("Error send BatchModuleServer BATCH_INFO");
			}
		}else {
			// 배치 삭제
			BatchGw batchGw = new BatchGw();
			batchGw.setBatchServiceId(batchServiceId);
			batchGw.setDeleteFlag(true);
			batchGwMapper.updateBatchServiceGw(batchGw);
		}
		
		return resultJson;
	}

	/**
	 * zip파일 압축 풀고 zip파일 삭제
	 * @param filePath
	 * @param fileName
	 * @throws Exception
	 */
	public void unzipAndDeleteGw(String filePath, String fileName) throws Exception {
		File zipFile = new File(filePath+"\\"+fileName);
        File targetDir = new File(filePath);
        logger.info("--- unzipAndDelete unzip zipFile: "+zipFile+", targetDir: "+targetDir);
        ZipUtils.unzip(zipFile, targetDir, false);
        
        FileUtil.fileDelete(filePath+"\\"+fileName);
        logger.info("--- unzipAndDelete fileDelete: "+filePath+"\\"+fileName);
	}
	
	
	/**
	 * 배치서버 목록 조회
	 * @return
	 * @throws Exception 
	 */
	public JSONArray batchServersGw() throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = batchGwMapper.batchServersGw();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}
	
	/**
	 * 배치 서버 개별 조회
	 * @param batchInstancePk
	 * @return
	 */
	public JSONObject batchServerGw(String batchInstancePk) throws Exception {
		int batchInstanceId;
		try {
			batchInstanceId = Integer.parseInt(batchInstancePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> batchServerDetail = batchGwMapper.batchServerGw(batchInstanceId);
		if( batchServerDetail == null ) {
			return restFullReturnService.resourceNotFound("Not found batch Server");
		}
		return MakeUtil.nvlJson(JSONObject.fromObject(batchServerDetail));
	}

	/**
	 * 배치 시작/정지
	 * @param batch
	 * @return
	 */
	public JSONObject batchServicesStartAndStopGw(HttpSession session, BatchGw batchGw, String batchInstancePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		int batchInstanceId;
		try {
			batchInstanceId = Integer.parseInt(batchInstancePk);
			batchGw.setBatchInstanceId(batchInstanceId);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}

		/* 사용자 권한체크 */
		String userRole = ""+session.getAttribute("userRole");
		if( !"Analytics_Admin".equals(userRole) ) {
			return restFullReturnService.unauthorized("Unauthorized");
		}
		
		// 배치 시작/정지 수정
		batchGwMapper.updateBatchServiceUseFlagGw(batchGw);
		Map<String, Object> detail = batchGwMapper.batchServiceGw(batchInstanceId, null);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));

		/*************** 배치서버에 전송 *********************/
		String listUrl = null;
		if( batchGw.isRunning() ) {
			listUrl = analyticsBatchServerUrl+"/batchStart/"+batchInstanceId;
		}else { // end
			listUrl = analyticsBatchServerUrl+"/batchStop/"+batchInstanceId;
		}
			
		if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
			JSONObject batchServerHttpJson = httpService.httpServiceGET(listUrl, "");
			if( !"200".equals(batchServerHttpJson.get("type")) ) {
				resultJson.put("type", "http://citydatahub.kr/errors/InternalError");
				resultJson.put("title", "Internal Error");
				resultJson.put("detail", batchServerHttpJson.get("data"));
				logger.error("Error batchServer send "+batchGw.getBatchState()+" error...");
				throw new RuntimeException("Error batchServer send "+batchGw.getBatchState()+" error...");
			}
		}
		
		return resultJson;
	}

	/**
	 * 배치 로그 조회
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public JSONArray batchLogsGw(HttpSession session, String startDate, String endDate) {
		
		JSONArray jsonArr = new JSONArray();
		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";
		
		List<Map<String, Object>> list = batchGwMapper.batchLogsGw(userId, startDate, endDate);
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		
		return jsonArr;
	}

	/**
	 * 배치 개별 로그 조회
	 * @param logBatchSequencePk
	 * @return
	 */
	public JSONObject batchLogGw(HttpSession session, String logBatchSequencePk) {

		String userRole = ""+session.getAttribute("userRole");
		String userId = ""+session.getAttribute("userId");
		if( "Analytics_Admin".equals(userRole) ) userId = "";

		int logBatchId;
		try {
			logBatchId = Integer.parseInt(logBatchSequencePk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> batchLogDetail = batchGwMapper.batchLogGw(logBatchId, userId);
        if( batchLogDetail == null ) {
            return restFullReturnService.resourceNotFound("Not found batchLog");
        }
		return MakeUtil.nvlJson(JSONObject.fromObject(batchLogDetail));
	}
}
