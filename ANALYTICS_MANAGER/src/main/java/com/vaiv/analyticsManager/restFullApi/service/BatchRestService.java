package com.vaiv.analyticsManager.restFullApi.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.common.service.HttpService;
import com.vaiv.analyticsManager.common.utils.FileUtil;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.common.utils.ZipUtils;
import com.vaiv.analyticsManager.restFullApi.domain.Batch;
import com.vaiv.analyticsManager.restFullApi.domain.SearchData;
import com.vaiv.analyticsManager.restFullApi.mapper.BatchRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class BatchRestService {

	private static Logger logger = LoggerFactory.getLogger(SandboxRestService.class);
	
	@Autowired
	private BatchRestMapper batchRestMapper;
	
	@Autowired
	private SandboxRestService sandboxRestService;
	
	@Autowired
	private HttpService httpService;


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
	 * 배치 목록 조회
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServices() throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = batchRestMapper.batchServices();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("batchServices", jsonArr);
		return resultJson;
	}

	/**
	 * 배치 조회
	 * @param batchServiceSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchService(Integer batchServiceSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = batchRestMapper.batchService(batchServiceSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


	/**
	 * 배치 등록
	 * @param batch
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public JSONObject batchServicesAsPost(Batch batch) throws NumberFormatException, Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		
		// 배치 명 중복 체크
		if( batchRestMapper.checkBatchName(batch) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			
			/* 저장될 파일 위치 생성  ex)/data/parkingarea/parking{yyyyMMddHH}.json */
			String applyDataPath = batch.getApplyDataPath();
			if( !"/".equals(applyDataPath.substring(0,1)) )
				applyDataPath = "/"+applyDataPath;
			FileUtil.mkdir(nfsResultPath + applyDataPath);
			
			
			/****************************** 학습 모듈에 배치 전송 ************************************/
			// 인스턴스 내부IP 가져오기
			String ip = sandboxRestService.getInstanceIp(batch.getSandboxInstanceSequenceFk1());
			
			String listUrl = ip + "/batchInfo?model_id="+batch.getModelSequenceFk4();
			httpJson = httpService.httpServiceGET(listUrl, "");
			
			if( "200".equals(httpJson.get("type")) ) {
				
				// 배치신청 업데이트
				if( MakeUtil.isNotNullAndEmpty(batch.getBatchServiceRequestSequencePk()) ) {
					Batch batchRequest = new Batch();
					batchRequest.setBatchServiceRequestSequencePk(batch.getBatchServiceRequestSequencePk());
					batchRequest.setProgressState("done");
					// batchRestMapper.updateBatchServiceRequest(batchRequest);
				}
				
				// 배치 등록
				if( batch.getEnrollementId() == null ) batch.setEnrollementId(batch.getUserId());
				batchRestMapper.insertBatchServices(batch);
				
				
				/*************** 배치분석모듈에 BATCH_INFO 전송 *********************/
				String batchIp = sandboxRestService.getInstanceIp(batch.getBatchInstanceSequenceFk2());
				
				listUrl = batchIp+"/batchService";
				JSONObject batchInfoParam = JSONObject.fromObject(httpJson.get("data"));
				batchInfoParam.put("BATCH_SERVICE_SEQUENCE_PK", batch.getBatchServiceSequencePk());
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
				String filePath = nfsPath+batch.getBatchServiceSequencePk();
				String fileName = "preprocessedData_"+batch.getBatchServiceSequencePk()+".zip";
				httpService.httpServiceDownloader(listUrl, filePath, fileName);
				// zip파일 압축 풀고 zip파일 삭제
				unzipAndDelete(filePath, fileName);
				
				// 모델
				listUrl = ip + "/models/"+batchInfoParam.get("MODEL_SEQUENCE_FK1")+"/download";
				filePath = nfsPath+batch.getBatchServiceSequencePk();
				fileName = "models_"+batch.getBatchServiceSequencePk()+".zip";
				httpService.httpServiceDownloader(listUrl, filePath, fileName);
				// zip파일 압축 풀고 zip파일 삭제
				unzipAndDelete(filePath, fileName);
				logger.info("preprocessedData, model Files save complete...");

				Map<String, Object> detail = batchRestMapper.batchService(batch.getBatchServiceSequencePk());
				if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
				
				resultJson.put("result", "success");
				resultJson.put("type", "2001");
				
			}else {
				resultJson.put("result", "error");
				resultJson.put("type", "5000");
			}
			return resultJson;
		}
	}

	/**
	 * 배치 수정
	 * @param batch
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServicesAsPatch(Batch batch) throws Exception {
		JSONObject resultJson = new JSONObject();
		// 배치 명 중복 체크
		if( batchRestMapper.checkBatchName(batch) > 0 ) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;
			
		}else {
			// 배치 수정
			batchRestMapper.updateBatchService(batch);
			Map<String, Object> detail = batchRestMapper.batchService(batch.getBatchServiceSequencePk());
			if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
			
			/*************** 배치서버에 전송 *********************/
			if( "start".equals(detail.get("BATCH_STATE")) ) {
				String listUrl = analyticsBatchServerUrl+"/batchStart/"+batch.getBatchServiceSequencePk();
				if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
					JSONObject analyticsBatchServerHttpJson = httpService.httpServiceGET(listUrl, "");
					if( "200".equals(analyticsBatchServerHttpJson.get("type")) ) {
						resultJson.put("result", "success");
						resultJson.put("type", "2001");
						logger.info("AnalyticsBatchServer send "+batch.getBatchState()+" complete...");
					}else {
						resultJson.put("result", "fail");
						resultJson.put("type", "5000");
						resultJson.put("detail", analyticsBatchServerHttpJson.get("data"));
						logger.error("Error AnalyticsBatchServer send "+batch.getBatchState()+" error...");
					}
				}else {
					resultJson.put("result", "success");
					resultJson.put("type", "2001");
					logger.info("AnalyticsBatchServer send complete...");
				}
			}else { // end
				resultJson.put("result", "success");
				resultJson.put("type", "2001");
			}
			
			return resultJson;
		}
	}

	/**
	 * 배치 삭제
	 * @param batchServiceSequencePk
	 * @return
	 * @throws Exception 
	 */
	public JSONObject batchServicesAsDelete(Integer batchServiceSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		
		/*************** 배치서버에 전송 *********************/
		if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
			String listUrl = analyticsBatchServerUrl+"/batchStop/"+batchServiceSequencePk;
			JSONObject analyticsBatchServerHttpJson = httpService.httpServiceGET(listUrl, "");
			if( "200".equals(analyticsBatchServerHttpJson.get("type")) ) {
				// 배치 삭제
				Batch batch = new Batch();
				batch.setBatchServiceSequencePk(batchServiceSequencePk);
				batch.setDeleteFlag(true);
				batchRestMapper.updateBatchService(batch);
				logger.info("AnalyticsBatchServer send complete...");
			}else {
				throw new RuntimeException("Error send AnalyticsBatchServer");
			}
		}else {
			// 배치 삭제
			Batch batch = new Batch();
			batch.setBatchServiceSequencePk(batchServiceSequencePk);
			batch.setDeleteFlag(true);
			batchRestMapper.updateBatchService(batch);
		}
		
		resultJson.put("result", "success");
		resultJson.put("type", "2001");
		return resultJson;
	}

	/**
	 * zip파일 압축 풀고 zip파일 삭제
	 * @param filePath
	 * @param fileName
	 * @throws Exception
	 */
	public void unzipAndDelete(String filePath, String fileName) throws Exception {
		File zipFile = new File(filePath+"\\"+fileName);
        File targetDir = new File(filePath);
        logger.info("--- unzipAndDelete unzip zipFile: "+zipFile+", targetDir: "+targetDir);
        ZipUtils.unzip(zipFile, targetDir, false);
        
        FileUtil.fileDelete(filePath+"\\"+fileName);
        logger.info("--- unzipAndDelete fileDelete: "+filePath+"\\"+fileName);
	}

	/**
	 * 배치 시작/정지
	 * @param batch
	 * @return
	 */
	public JSONObject batchServicesStartAndStop(Batch batch) throws Exception {
		JSONObject resultJson = new JSONObject();
			
		// 배치 시작/정지 수정
		batchRestMapper.updateBatchServiceUseFlag(batch);
		Map<String, Object> detail = batchRestMapper.batchService(batch.getBatchServiceSequencePk());
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchService", MakeUtil.nvlJson(JSONObject.fromObject(detail)));

		/*************** 배치서버에 전송 *********************/
		String listUrl = null;
		if( batch.isUseFlag() ) {
			listUrl = analyticsBatchServerUrl+"/batchStart/"+batch.getBatchServiceSequencePk();
		}else { // end
			listUrl = analyticsBatchServerUrl+"/batchStop/"+batch.getBatchServiceSequencePk();
		}
			
		if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
			JSONObject analyticsBatchServerHttpJson = httpService.httpServiceGET(listUrl, "");
			if( "200".equals(analyticsBatchServerHttpJson.get("type")) ) {
				resultJson.put("result", "success");
				resultJson.put("type", "2001");
				logger.info("AnalyticsBatchServer send "+batch.getBatchState()+" complete.");
			}else {
				resultJson.put("result", "fail");
				resultJson.put("type", "5000");
				resultJson.put("detail", analyticsBatchServerHttpJson.get("data"));
				logger.error("Error AnalyticsBatchServer send "+batch.getBatchState()+" error...");
				throw new RuntimeException("Error AnalyticsBatchServer send "+batch.getBatchState()+" error...");
			}
		}else {
			resultJson.put("result", "fail");
			resultJson.put("type", "5000");
			logger.info("Error AnalyticsBatchServer send");
		}
		
		return resultJson;
	}

	public JSONObject batchServicesUpdate(Integer batchServiceSequence) throws Exception {
		JSONObject resultJson = new JSONObject();

		// 배치 업데이트
		/*************** 배치서버에 전송 *********************/
		String listUrl = null;

		listUrl = analyticsBatchServerUrl+"/batchStart/"+batchServiceSequence;
		if( MakeUtil.isNotNullAndEmpty(analyticsBatchServerUrl) ) {
			JSONObject analyticsBatchServerHttpJson = httpService.httpServiceGET(listUrl, "");
			if( "200".equals(analyticsBatchServerHttpJson.get("type")) ) {
				resultJson.put("result", "success");
				resultJson.put("type", "2001");
				logger.info("AnalyticsBatchServer send "+batchServiceSequence+" update...");
			}else {
				resultJson.put("result", "fail");
				resultJson.put("type", "5000");
				resultJson.put("detail", analyticsBatchServerHttpJson.get("data"));
				logger.error("Error AnalyticsBatchServer send "+batchServiceSequence+" update error...");
				throw new RuntimeException("Error AnalyticsBatchServer send "+batchServiceSequence+" error...");
			}
		}else {
			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			logger.info("AnalyticsBatchServer send complete...");
		}

		return resultJson;
	}


	/**
	 * 배치 로그 조회
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String batchLogs(SearchData searchData) {
		JSONObject resultJson = new JSONObject();
		
		
		
		// sort 컬럼명 지정
		String[] columns = searchData.getColumns();

		if(columns != null){
			searchData.setSSortCol(columns[searchData.getISortCol_0()]);
			
			List<Map<String, Object>> list = batchRestMapper.batchLogs(searchData);
			int searchTotalCount = batchRestMapper.batchLogsSearchTotalCount(searchData);
			int totalCount = batchRestMapper.batchLogsTotalCount(searchData);
			
			resultJson.put("aaData", list);
			resultJson.put("iTotalDisplayRecords", searchTotalCount);
			resultJson.put("iTotalRecords", totalCount);
		}
			
			return resultJson.toString();
	}

	/**
	 * 배치 개별 로그 조회
	 * @param logBatchSequencePk
	 * @return
	 */
	public JSONObject batchLog(Integer logBatchSequencePk) {
		JSONObject resultJson = new JSONObject();
		
		Map<String, Object> detail = batchRestMapper.batchLog(logBatchSequencePk);
		if( MakeUtil.isNotNullAndEmpty(detail) )	resultJson.put("batchLog", MakeUtil.nvlJson(JSONObject.fromObject(detail)));
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


}
