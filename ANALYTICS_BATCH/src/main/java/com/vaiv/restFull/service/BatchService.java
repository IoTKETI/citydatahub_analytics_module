package com.vaiv.restFull.service;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.PostConstruct;

import com.vaiv.common.service.HttpService;
import com.vaiv.common.utils.MakeUtil;
import com.vaiv.restFull.domain.BatchServiceState;
import com.vaiv.restFull.mapper.BatchMapper;

@Service
public class BatchService {

	@Autowired
	private BatchMapper batchMapper;
	
	@Autowired
	private HttpService httpService;
	
	@Value("${nfs.resultPath}")
	private String nfsResultPath;
	
	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	@Value("${batchServer.devTest}")
	private boolean batchServerDevTest;
	
	@Value("${batchServer.url}")
	private String batchServerUrl;
	
	@Value("${coreModuleApiServer.url}")
	private String coreModuleApiServerUrl;
	
	@Value("${coreModuleApiServer.method}")
	private String coreModuleApiServerMethod;

	@Value("${coreModuleIngestServer.url}")
	private String coreModuleIngestServerUrl;

	@Value("${coreModuleIngestServer.datasetId}")
	private String ingestDatasetId;

	@Value("${coreModuleIngestServer.type}")
	private String ingestType;

	private final String CODE_BATCH_SERVICE_FAIL="error";
	private final String CODE_BATCH_SERVICE_SUCCESS="success";
	
	private static Logger logger = LoggerFactory.getLogger(BatchService.class);
	private static final String BATCH_KEY = "batchSequence_";
	
    private Map<String, String> batchKeyAndExecutionCycleMap;
    private Map<String, ScheduledFuture<?>> batchKeyAndFutureFunctionMap;
    private TaskScheduler scheduler;
    
    public BatchService(TaskScheduler scheduler) {
    	this.scheduler = scheduler;
		batchKeyAndExecutionCycleMap = new HashMap<String, String>();
		batchKeyAndFutureFunctionMap = new HashMap<String, ScheduledFuture<?>>();
    }

    @PostConstruct
	public void postConstruct()  {
		//1. 배치관리서버가 실행되면 디비에 있는 정보를 읽어와서 배치를 실행시킴
    	logger.info("1. 배치관리서버가 실행되면 디비에 있는 정보를 읽어와서 배치를 등록함");
		List<Map<String, Object>> batchServices = batchMapper.batchServicesAll();
		for (Map<String, Object> batchService : batchServices) {
			logger.info("등록한 배치서버 아이디" + batchService.get("BATCH_SERVICE_SEQUENCE_PK").toString());
			try {
				batchStart(Long.parseLong("" + batchService.get("BATCH_SERVICE_SEQUENCE_PK")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 배치 다시 시작
	 * @param sandboxInstanceSequence
	 * @return
	 * @throws Exception
	 */
	public JSONObject refrash(Long sandboxInstanceSequence) throws Exception {
		logger.info("+++ refrash +++ sandboxInstanceSequence: "+sandboxInstanceSequence);
		JSONObject resultJson = new JSONObject();
		
		// 실행하고 있는거 모두 중지
		allStop(sandboxInstanceSequence);
		
		List<Map<String, Object>> batchServices = batchMapper.batchServices(sandboxInstanceSequence);
		for (Map<String, Object> batchService : batchServices) {
			batchStart(Long.parseLong(""+batchService.get("BATCH_SERVICE_SEQUENCE_PK")));
		}

		logger.info("+++ refrash +++ refrash Complete sandboxInstanceSequence: "+sandboxInstanceSequence);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


	/**
	 * 배치 모두 중지
	 * @param sandboxInstanceSequence
	 * @return
	 */
	public JSONObject allStop(Long sandboxInstanceSequence) throws Exception {
		logger.info("--- allStop --- sandboxInstanceSequence: "+sandboxInstanceSequence);
		JSONObject resultJson = new JSONObject();
		String key;
		
		List<Map<String, Object>> batchServices = batchMapper.batchServices(sandboxInstanceSequence);
		for (Map<String, Object> batchService : batchServices) {
			key = BATCH_KEY+batchService.get("BATCH_SERVICE_SEQUENCE_PK");
			if( MakeUtil.isNotNullAndEmpty(batchKeyAndFutureFunctionMap.get(key)) ) {
				logger.info("stopped job key :{}",key);
				batchKeyAndFutureFunctionMap.get(key).cancel(true);
				batchKeyAndExecutionCycleMap.remove(key);
				batchKeyAndFutureFunctionMap.remove(key);
		    	logger.info("--- allStop --- stop Complete key: "+key);
			}else {
				logger.info("--- allStop --- Not running job key: "+key);
			}
		}

		logger.info("--- allStop --- allStop Complete sandboxInstanceSequence: "+sandboxInstanceSequence);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


	/**
	 * 배치 개별 시작
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public JSONObject batchStart(Long batchServiceSequencePk) throws NumberFormatException, Exception {
		logger.info("---- batchStart 요청을 받음 ( batchServiceSequencePk: "+batchServiceSequencePk+" )");

		JSONObject resultJson = new JSONObject();
		String batchKey, executionCycle;

		// 실행되고 있다면...중지
		batchStop(batchServiceSequencePk);
		
		Map<String, Object> batchServiceInfoMap = batchMapper.batchService(batchServiceSequencePk);

		if( batchServiceInfoMap!=null && MakeUtil.isNotNullAndEmpty(batchServiceSequencePk)) {
			logger.info("A. Batch 정보 : "+batchServiceInfoMap.toString());
			batchKey = BATCH_KEY+batchServiceInfoMap.get("BATCH_SERVICE_SEQUENCE_PK");
			executionCycle = "0 "+batchServiceInfoMap.get("EXECUTION_CYCLE");

			if(batchKeyAndExecutionCycleMap.containsKey(batchKey)) {
				return resultJson;
			}

			batchKeyAndExecutionCycleMap.put(batchKey,executionCycle);
			logger.info("B. 등록한 배치정보 (참고 전체 배치목록 /list 호출)");
	    	logger.info("B.1 등록된 배치 KEY 정보 :"+batchKey);
			logger.info("B.2 등록된 배치 KEY 주기정보 :"+executionCycle);
	        ScheduledFuture<?> future = this.scheduler.schedule(() -> {
				try {
					logger.info("---- 배치작업 실행 ( batchServiceSequencePk: "+batchServiceSequencePk+" )");

		        	String applyDataPath = ""+batchServiceInfoMap.get("APPLY_DATA_PATH");
		        	String applyDataNameRule = ""+batchServiceInfoMap.get("APPLY_DATA_NAME_RULE");

					if(applyDataNameRule.contains("{")){
						String applyDataRule = applyDataNameRule.substring(applyDataNameRule.indexOf("{")+1,applyDataNameRule.indexOf("}")); // yyyyMMddHH
						SimpleDateFormat sdf = new SimpleDateFormat(applyDataRule);
						String applyDate = sdf.format(new Date());
						applyDataNameRule = applyDataNameRule.replace("{"+applyDataRule+"}", applyDate);
					}
		        	
		        	String applyDataFullPath =nfsResultPath + (applyDataPath.endsWith("/")?applyDataPath:applyDataPath+"/") + applyDataNameRule;
		        	logger.info("A. 배치 대상파일 경로 : "+applyDataFullPath);

					BatchLogService batchLogService=new BatchLogService();

					batchLogService.setBatchServiceSequence(batchServiceSequencePk);
					batchLogService.setBatchInstanceSequence((Long)batchServiceInfoMap.get("SANDBOX_INSTANCE_SEQUENCE_FK1"));

					String updateAttriubte=""+batchServiceInfoMap.get("UPDATE_ATTRIBUTE"); //"congestionIndexPrediction";
					String resultUpdateMethod=""+batchServiceInfoMap.get("RESULT_UPDATE_METHOD"); //"update"; //replace or update

 					String totalColumnName=""+batchServiceInfoMap.get("TOTAL_SPOT_NUMBER"); //"totalspotnumber";
					String domainIdColumnName=""+batchServiceInfoMap.get("DOMAIN_ID_COLUMN_NAME"); //"parking_id";
                    String timeColumnName=""+batchServiceInfoMap.get("TIME_COLUMN_NAME"); //"parking_id";
					String storeMethod=""+batchServiceInfoMap.get("STORE_METHOD"); //"index";
					String targetType=""+batchServiceInfoMap.get("TARGET_TYPE"); //"percentage, rawData";

					doBatch(batchLogService, batchServiceInfoMap, batchServiceSequencePk, nfsResultPath, coreModuleApiServerUrl, coreModuleIngestServerUrl, updateAttriubte, resultUpdateMethod, totalColumnName, domainIdColumnName, timeColumnName,  storeMethod, applyDataFullPath, executionCycle, targetType);


				} catch (Exception e) {
					logger.error("배치를 실행을 실패하였습니다. ("+batchServiceSequencePk+") : "+e.getMessage());
					e.printStackTrace();
				}
            },
            new CronTrigger(batchKeyAndExecutionCycleMap.get(batchKey)));
			batchKeyAndFutureFunctionMap.put(batchKey,future);
		}else{
			logger.error("등록된 배치정보가 없습니다. ("+batchServiceSequencePk+") ");

			resultJson.put("result", "fail");
			resultJson.put("type", "5000");
			return resultJson;
		}
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}


	/**
	 * 배치 개별 중지
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	public JSONObject batchStop(Long batchServiceSequencePk) throws Exception {
		logger.info("--- batchStop --- batchServiceSequencePk: "+batchServiceSequencePk);
		JSONObject resultJson = new JSONObject();
		String key;
		
		Map<String, Object> batchService = batchMapper.batchService(batchServiceSequencePk);
		if( MakeUtil.isNotNullAndEmpty(batchService) ) {
			key = BATCH_KEY+batchService.get("BATCH_SERVICE_SEQUENCE_PK");
			if( MakeUtil.isNotNullAndEmpty(batchKeyAndFutureFunctionMap.get(key)) ) {
				logger.info("stopped job key :{}",key);
				batchKeyAndFutureFunctionMap.get(key).cancel(true);
				batchKeyAndExecutionCycleMap.remove(key);
				batchKeyAndFutureFunctionMap.remove(key);
		    	logger.info("--- batchStop --- stop Complete key: "+key);
			}else {
				logger.info("--- batchStop --- Not running job key: "+key);
			}
		}else{
			logger.error("등록된 배치정보가 없습니다. ("+batchServiceSequencePk+") ");

			resultJson.put("result", "fail");
			resultJson.put("type", "5000");
			return resultJson;
		}
		
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}
	
	public String getModuleURL(Long instanceSequencePk) throws Exception {
		Map<String, Object> instance = batchMapper.instance(instanceSequencePk);
		String ip = "http://" + instance.get("PRIVATE_IP") + ":" + modulePort + moduleMethod;
		
		if( MakeUtil.isNotNullAndEmpty(moduleTempUrl) )	ip = moduleTempUrl + ":" + modulePort + moduleMethod;
		
		return ip;
	}

	public JSONObject list() {
		logger.info("------ 현재 등록되어 있는 cronMap --------------");

		for(String key : batchKeyAndExecutionCycleMap.keySet() ){
			logger.info("key : "+key);
			logger.info("ExecutionCycle : "+batchKeyAndExecutionCycleMap.get(key));

		}
		logger.info("------ 현재 등록되어 있는 futureMap --------------");
		for(String key : batchKeyAndFutureFunctionMap.keySet() ){
			logger.info(key);
			logger.info("Function" + batchKeyAndFutureFunctionMap.get(key));
		}

		return new JSONObject();
	}

	public void doBatch(BatchLogService batchLogService, Map<String, Object>  batchService, Long batchServiceSequencePk, String nfsResultPath, String coreModuleApiServerUrl, String coreModuleIngestServerUrl, String updateAttribute, String resultUpdateMethod, String totalColumnName, String domainIdColumnName, String timeColumnName,  String storeMethod, String applyDataFullPath, String executionCycle, String targetType){
		LocalDateTime batchStartDatetime= LocalDateTime.now();

		batchLogService.writeLogAboutBatchStart(batchStartDatetime, storeMethod , updateAttribute, resultUpdateMethod, executionCycle);
		batchStartDatetime= LocalDateTime.now();

		HttpService httpService=new HttpService();

		try {
			logger.info("B. 배치작업을 실행 ");

			String applyDataFullPathForModule=applyDataFullPath;
			logger.info("B.1. 적용할 데이터를 로드");
			logger.info("B.1.1. applyDataFullPathManager를 위한 경로 : "+applyDataFullPath);
			logger.info("B.1.1. applyDataFullPathForModule을 위한 경로 : "+applyDataFullPathForModule);

			File applyDataFile = new File(applyDataFullPath);


			if( applyDataFile.exists() ) {

				JSONArray applyDatasJson = new JSONArray();
				JSONObject httpJson = null;

				batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath, applyDataFile.length(), true, "");
				logger.info("B.2. 배치용 서버 분석 모듈에서 배치를 실행");
				batchStartDatetime= LocalDateTime.now();

				// 2. 배치작업을 실행할 배치용 서버 인스턴스 정보를 가져옴
				String moduleURL = getModuleURL(Long.parseLong(""+batchService.get("BATCH_INSTANCE_SEQUENCE_FK2")));
				if( batchServerDevTest ) {
					moduleURL = batchServerUrl;
				}

				String batchCommandUrl = moduleURL +"/batchService/"+ batchService.get("BATCH_SERVICE_SEQUENCE_PK");

				logger.info("B.2.1 배치실행 명령어 : "+batchCommandUrl);
				batchLogService.writeLogAboutBatchExecute(batchStartDatetime,true, "");

				batchStartDatetime= LocalDateTime.now();
				JSONObject param = new JSONObject();
				param.put("test_data_path", applyDataFullPathForModule);

				httpJson = httpService.httpServicePATCH(batchCommandUrl, param.toString());

				logger.info("B.2.2 배치실행 결과값 : "+httpJson.toString());

				// 성공시
				if ("200".equals(httpJson.get("type"))) {
                    JSONObject params = null;
					JSONObject entityFormat=null;

                    JSONObject data = new JSONObject().fromObject(httpJson.get("data"));

					Scanner scan = new Scanner(applyDataFile);
					JSONObject contentJson = null;
					String content = null;

					while (scan.hasNextLine()) {
						content = scan.nextLine();
						applyDatasJson.add(contentJson.fromObject(content));
					}

					if (applyDatasJson.isEmpty()) {
						batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
						batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath, applyDataFile.length(), false, "배치를 위한 데이터가 존재하지 않습니다.");
						logger.error("배치를 위한 데이터가 존재하지 않습니다.("+batchServiceSequencePk+") : "+applyDataFullPathForModule);
						return;
					}
					JSONArray sendJsonArr;
                    /// PostProceesing
					logger.info("B.3.1 후처리를 실행 ");
                    sendJsonArr=makePostProcessingArray(storeMethod, applyDatasJson, data, totalColumnName, timeColumnName, domainIdColumnName);

					logger.info("B.4 index를 domainIdColumnName과 timeColumnName sorting");
					// 데이터를 정렬
					sendJsonArr = MakeUtil.sortJsonArrWithTwoKey(sendJsonArr, domainIdColumnName, "predictedAt", "asc", "asc");

					logger.info("=========== " + batchServiceSequencePk + " batch process =========== sendJsonArr: " + sendJsonArr.toString());

					// 결과 코어 모듈에 전송
                    HashMap<String, JSONObject> resultMap=makeCoreStoreMap(sendJsonArr,domainIdColumnName, updateAttribute);

					batchLogService.writeLogAboutBatchExecuteResult(batchStartDatetime, true, "", httpJson.toString());
					batchStartDatetime= LocalDateTime.now();

					logger.info("B.5 결과값을 코어모듈에 저장 : "+resultUpdateMethod);
					String saveUrl=null;

					try {
						//결과값을 코어모듈에 저장
						JSONArray paramsArray=new JSONArray();

						for (String key : resultMap.keySet()) {
							params = resultMap.get(key);
							saveUrl = coreModuleIngestServerUrl;

							if (resultUpdateMethod.equals("update")) {
								String getUrl = coreModuleIngestServerUrl+"/" + key;
								params = getDataFromDataCoreAndUpdateNewlySettedParams(httpService, getUrl, params, storeMethod);
							}

							JSONObject paramoneJsonObject=new JSONObject();
							paramoneJsonObject.put("id", key);
							paramoneJsonObject.put("type", ingestType);
							paramoneJsonObject.put("predictions", params.get("predictions"));
							paramoneJsonObject.put("@context", Arrays.asList(new String [] {"http://uri.citydatahub.kr/ngsi-ld/v1/context.jsonld", "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"}));
							paramsArray.add(paramoneJsonObject);

						}

						entityFormat=new JSONObject();
						entityFormat.put("datasetId", ingestDatasetId);
						entityFormat.put("entities", paramsArray);

						httpJson = httpService.httpServicePOST(saveUrl, entityFormat);
						batchLogService.writeLogAboutBatchTransferResult(batchStartDatetime, true,"", entityFormat.toString().replaceAll("'\\{","{"), saveUrl);
						batchStartDatetime= LocalDateTime.now();
						logger.info("=========== " + batchServiceSequencePk + " batch process =========== httpJson: " + httpJson.toString());

						batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_SUCCESS));

					}catch (Exception e){
						batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
						e.printStackTrace();
						batchLogService.writeLogAboutBatchTransferResult(batchStartDatetime, false, httpJson.toString(), entityFormat.toString().replaceAll("'\\{","{"), saveUrl);
					}

				} else {
					batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
					batchLogService.writeLogAboutBatchExecuteResult(batchStartDatetime, false, httpJson.toString(),"");
					logger.info("=========== " + batchServiceSequencePk + " batch process =========== failed at batch module: " );
				}
			} else {
				batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
				batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath, applyDataFile.length(), false, "배치 대상이 되는 파일이 존재하지 않습니다.");
				logger.info("=========== " + batchServiceSequencePk + " batch process =========== Not Found fileFullName: " + applyDataFullPathForModule);
			}
		} catch (Exception e) {
			batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
			logger.error("=========== "+batchServiceSequencePk+" batch process ===========",e);
			e.printStackTrace();
		}
		batchLogService.writeLogAboutBatchEnd(batchStartDatetime);
		return ;

	}

	private JSONObject getDataFromDataCoreAndUpdateNewlySettedParams(HttpService httpService,  String getUrl, JSONObject updateData, String updateAttriubte) throws Exception {
		JSONObject getResult=httpService.httpServiceGET(getUrl);

		JSONObject domainInformation=(JSONObject)getResult.get("data");
	  	JSONArray storedValue=(JSONArray)((JSONObject)domainInformation.get(updateAttriubte)).get("value");
		JSONArray newValue=(JSONArray)((JSONObject)updateData.get("predictions")).get("value");
		  
		HashMap<String, Object> predictMap=new HashMap<String, Object>();

		for(Object indexPredictionObject:storedValue){
			JSONObject indexPredictionJson= ((JSONObject)indexPredictionObject);
			predictMap.put((String)indexPredictionJson.get("predictedAt"), indexPredictionJson.get("index"));
		}

		for(Object indexPredictionObject:newValue){
			JSONObject indexPredictionJson= ((JSONObject)indexPredictionObject);
			predictMap.put((String)indexPredictionJson.get("predictedAt"), indexPredictionJson.get("index"));
		}

		ArrayList<String> predictAtList=new ArrayList<String>();

		for(String key : predictMap.keySet()){
			predictAtList.add(key);
		}

		Collections.sort(predictAtList);

		JSONArray updateValue=new JSONArray();

		for(String key: predictAtList){
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("predictedAt", key);
			jsonObject.put("index", predictMap.get(key));
			updateValue.add(jsonObject);
		}

		((JSONObject)updateData.get(updateAttriubte)).put("value", updateValue);

		return updateData;
	}

	
	private   HashMap<String, JSONObject> makeCoreStoreMap(JSONArray sendJsonArr, String domainIdColumnName, String updateAttribute){
        HashMap<String, JSONObject> resultMap = new HashMap<String, JSONObject>();

        String targetEntityId = "";
        JSONObject sendJson =null;
        JSONObject params = null;
        JSONObject congestionIndexPrediction = null;

        JSONArray tempJsonArray = new JSONArray();
        JSONObject tempJson = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");

        HashSet<String> updateAttributeMap=new HashSet<String>();


		for(String attribute : updateAttribute.split(","))
		{
			updateAttributeMap.add(attribute);
		}

        for (int i = 0; i < sendJsonArr.size(); i++) {
            sendJson = new JSONObject().fromObject(sendJsonArr.get(i));
            JSONObject inputData=JSONObject.fromObject(sendJson.get("inputData").toString().replace("'",""));

			if ("".equals(targetEntityId) || targetEntityId.equals(inputData.get(domainIdColumnName))) {
				  targetEntityId = "" + inputData.get(domainIdColumnName);
                tempJson = new JSONObject();
                for(Object key : sendJson.keySet()){
                    String keyStr=(String)key.toString();
                    if(updateAttributeMap.contains(keyStr)){
						tempJson.put(keyStr, sendJson.get(keyStr));
					}
                }
                tempJsonArray.add(tempJson);
            } else {
                //resultMap에 저장
                congestionIndexPrediction = new JSONObject();
                params = new JSONObject();
                congestionIndexPrediction.put("type", "Property");
                congestionIndexPrediction.put("value", tempJsonArray);
				if(updateAttributeMap.contains("observedAt")){
					congestionIndexPrediction.put("observedAt", sdf.format(new Date()));
				}

                params.put("predictions", congestionIndexPrediction);
                resultMap.put(targetEntityId, params);

                targetEntityId = "" + inputData.get(domainIdColumnName);
                tempJsonArray = new JSONArray();
                tempJson = new JSONObject();
                for(Object key : sendJson.keySet()){
                    String keyStr=(String)key.toString();
					if(updateAttributeMap.contains(keyStr)){
						tempJson.put(keyStr, sendJson.get(keyStr));
					}
                }
                tempJsonArray.add(tempJson);
            }

            if (i == (sendJsonArr.size() - 1)) {
                congestionIndexPrediction = new JSONObject();
                params = new JSONObject();
                congestionIndexPrediction.put("type", "Property");
                congestionIndexPrediction.put("value", tempJsonArray);
				if(updateAttributeMap.contains("observedAt")){
					congestionIndexPrediction.put("observedAt", sdf.format(new Date()));
				}
                params.put("predictions", congestionIndexPrediction);
                resultMap.put(targetEntityId, params);
            }
        }
	    return resultMap;
    }

	private JSONArray makePostProcessingArray(String storeMethod, JSONArray applyDataJsonList, JSONObject predictResultJson, String totalColumnName, String targetType, String domainIdColumnName) throws Exception {
        //////--> 여기서부터 수정
        // 결과 세팅
        JSONArray sendJsonArr=new JSONArray();
        long percentage = 0;
        double totalspotnumber = -1;


		String predictString="";
        // 적용할 데이터에서의 값과 예측값을 가지고 인덱스값을 생성
        List<String> predictList = (List<String>) predictResultJson.get("predict");

        JSONObject applyDataJson = null;
        JSONObject sendJson =null;
        String totalColumnString=null;

		for (int i = 0; i < applyDataJsonList.size(); i++) {
			sendJson=new JSONObject();
 			applyDataJson = JSONObject.fromObject(applyDataJsonList.get(i));

			sendJson.put("inputData","'"+applyDataJson.toString());

			if(applyDataJson.get("predictedAt")!=null){
				sendJson.put("predictedAt", applyDataJson.get("predictedAt"));
			}
			predictString=predictList.get(i);
			sendJson.put("predictValue", predictString);

			if(storeMethod.equals("percentage")){
				float predictFloat;

				if (totalspotnumber != 0.0) {
                    totalColumnString=applyDataJson.get(totalColumnName).toString();

                    if(totalColumnString==null){
                        throw new Exception("Total ColumnName이 존재하지 않습니다. ");
                    }
					totalspotnumber = Double.parseDouble(totalColumnString);
					predictFloat = Float.parseFloat(predictList.get(i));

					percentage = Math.round((predictFloat * 1.0f) / totalspotnumber * 100); // (totalspotnumber - 예측값) / totalspotnumber *100
					sendJson.put("percentage", percentage);

				}else{
					logger.info(applyDataJson.get(domainIdColumnName)+"의 totalsponumber가 0입니다.");
					sendJson.put("percentage", -1);
				}
			}


			sendJsonArr.add(sendJson);


		}
		return sendJsonArr;
    }
}