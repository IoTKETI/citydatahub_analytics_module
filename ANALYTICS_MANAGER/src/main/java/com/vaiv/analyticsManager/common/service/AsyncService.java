package com.vaiv.analyticsManager.common.service;

import java.net.ConnectException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.restFullApi.domain.Model;
import com.vaiv.analyticsManager.restFullApi.domain.PreprocessedData;
import com.vaiv.analyticsManager.restFullApi.mapper.ProjectRestMapper;

import net.sf.json.JSONObject;

@Service
@Async("threadPoolTaskExecutor")
public class AsyncService {
	private Logger logger = LoggerFactory.getLogger(HttpService.class);
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private ProjectRestMapper projectRestMapper;
	
	@Value("${module.asyncSecond}")
	private Integer asyncSecond;
	
	@Value("${module.asyncPeriod}")
	private Integer asyncPeriod;
	
	private static int limitFailCnt = 5;
	private int connectionFailCnt;

	/**
	 * 전처리 생성
	 * @param url
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	@SuppressWarnings("static-access")
	public Future<String> preprocessedData(String url, Integer preprocessedDataSequencePk){
		logger.info("AsyncService-preprocessedData : Start!!! ");
		JSONObject httpJson = null;
		JSONObject preprocessedDataJson = null;
		boolean result = false;
		connectionFailCnt = 0;
		try {
			for (int i = 0; i < asyncPeriod; i++) {
				try {
					httpJson = httpService.httpServiceGET(url, "");
					preprocessedDataJson = new JSONObject().fromObject(httpJson.get("data"));	
				} catch (ConnectException e) {
					connectionFailCnt++;
					logger.info("AsyncService-preprocessedData connectionFailCnt: "+connectionFailCnt+", Error: "+e);
					if( connectionFailCnt > limitFailCnt)	break;
				}
				
				if( !"ongoing".equals(preprocessedDataJson.get("PROGRESS_STATE")) ) {
					PreprocessedData pData = new PreprocessedData();
					pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
					pData.setFilepath(""+preprocessedDataJson.get("FILEPATH"));
					pData.setFilename(""+preprocessedDataJson.get("FILENAME"));
					pData.setSummary(""+preprocessedDataJson.get("SUMMARY"));
					pData.setProgressState(""+preprocessedDataJson.get("PROGRESS_STATE"));
					pData.setProgressEndDatetime(""+preprocessedDataJson.get("PROGRESS_END_DATETIME"));
					pData.setColumns(""+preprocessedDataJson.get("COLUMNS"));
					pData.setStatistics(""+preprocessedDataJson.get("STATISTICS"));
					pData.setSampleData(""+preprocessedDataJson.get("SAMPLE_DATA"));
					pData.setAmount(Integer.parseInt(""+preprocessedDataJson.get("AMOUNT")));
					projectRestMapper.updatePreprocessedData(pData);
					logger.info("AsyncService-preprocessedData : Complete preprecessedData");
					result = true;
					break;
				}
				logger.info("AsyncService-preprocessedData : i => "+i);
				Thread.sleep(asyncSecond);
			}
			if( !result ) {
				PreprocessedData pData = new PreprocessedData();
				pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
				pData.setProgressState("fail");
				projectRestMapper.updatePreprocessedData(pData);
				logger.info("AsyncService-preprocessedData : overTime");
			}
		} catch (Exception e) {
			logger.error("Error AsyncService-preprocessedData",e);
			
			try {
				PreprocessedData pData = new PreprocessedData();
				pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
				pData.setProgressState("fail");
				projectRestMapper.updatePreprocessedData(pData);
				logger.error("Error AsyncService-preprocessedData update",e);
				
			} catch (Exception e1) {
				logger.error("Error AsyncService-preprocessedData update",e);
			}
			
		}
		logger.info("AsyncService-preprocessedData : End!!! ");
		if( result )return new AsyncResult<String>("success");
		else return new AsyncResult<String>("false");
	}

	/**
	 * 모델 생성
	 * @param url
	 * @param modelSequencePk
	 */
	@SuppressWarnings("static-access")
	public Future<String> models(String url, Integer modelSequencePk) {
		logger.info("AsyncService-models : Start!!! ");
		JSONObject httpJson = null;
		JSONObject modelsJson = null;
		boolean result = false;
		connectionFailCnt = 0;
		try {
			for (int i = 0; i < asyncPeriod; i++) {
				try {
					httpJson = httpService.httpServiceGET(url, "");
					modelsJson = new JSONObject().fromObject(httpJson.get("data"));
					
				} catch (ConnectException e) {
					logger.info("AsyncService-models : "+e);
					connectionFailCnt++;
					logger.info("AsyncService-models connectionFailCnt: "+connectionFailCnt+", Error: "+e);
					if( connectionFailCnt > limitFailCnt)	break;
				}
				
				if( !"ongoing".equals(modelsJson.get("PROGRESS_STATE")) ) {
					Model model = new Model();
					model.setModelSequencePk(modelSequencePk);
					model.setFilepath(""+modelsJson.get("FILEPATH"));
					model.setFilename(""+modelsJson.get("FILENAME"));
					model.setTrainSummary(""+modelsJson.get("TRAIN_SUMMARY"));
					model.setValidationSummary(""+modelsJson.get("VALIDATION_SUMMARY"));
					model.setProgressState(""+modelsJson.get("PROGRESS_STATE"));
					model.setProgressEndDatetime(""+modelsJson.get("PROGRESS_END_DATETIME"));
					model.setLoadState(""+modelsJson.get("LOAD_STATE"));
					projectRestMapper.updateModels(model);
					logger.info("AsyncService-models : Complete preprecessedData");
					result = true;
					break;
				}
				Thread.sleep(asyncSecond);
			}
			if( !result ) {
				Model model = new Model();
				model.setModelSequencePk(modelSequencePk);
				model.setProgressState("standby");
				projectRestMapper.updateModels(model);
				logger.info("AsyncService-models : overTime");
			}
		} catch (Exception e) {
			try {
				Model model = new Model();
				model.setModelSequencePk(modelSequencePk);
				model.setProgressState("fail");
				projectRestMapper.updateModels(model);
			} catch (Exception e1) {
				logger.error("Error AsyncService-models update",e);
			}
			logger.error("Error AsyncService-models",e);
		}
		logger.info("AsyncService-models : End!!! ");
		if( result )return new AsyncResult<String>("success");
		else return new AsyncResult<String>("false");
		
	}
}
