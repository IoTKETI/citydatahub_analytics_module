package com.vaiv.restFull.service;

import java.io.File;
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

	@Value("${coreModuleIngestServer.url}")
	private String coreModuleIngestServerUrl;

	private final String CODE_BATCH_SERVICE_FAIL = "error";
	private final String CODE_BATCH_SERVICE_SUCCESS = "success";

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
	public void postConstruct() {
		// 1. ????????????????????? ???????????? ????????? ?????? ????????? ???????????? ????????? ????????????
		logger.info("1. ????????????????????? ???????????? ????????? ?????? ????????? ???????????? ????????? ?????????");
		List<Map<String, Object>> batchServices = batchMapper.batchServicesAll();
		for (Map<String, Object> batchService : batchServices) {
			logger.info("????????? ???????????? ?????????" + batchService.get("BATCH_SERVICE_SEQUENCE_PK").toString());
			try {
				batchStart(Long.parseLong("" + batchService.get("BATCH_SERVICE_SEQUENCE_PK")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ?????? ?????? ??????
	 * 
	 * @param sandboxInstanceSequence
	 * @return
	 * @throws Exception
	 */
	public JSONObject refresh(Long sandboxInstanceSequence) throws Exception {
		logger.info("+++ refresh +++ sandboxInstanceSequence: " + sandboxInstanceSequence);
		JSONObject resultJson = new JSONObject();

		// ???????????? ????????? ?????? ??????
		allStop(sandboxInstanceSequence);

		List<Map<String, Object>> batchServices = batchMapper.batchServices(sandboxInstanceSequence);
		for (Map<String, Object> batchService : batchServices) {
			batchStart(Long.parseLong("" + batchService.get("BATCH_SERVICE_SEQUENCE_PK")));
		}

		logger.info("+++ refresh +++ refresh Complete sandboxInstanceSequence: " + sandboxInstanceSequence);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * ?????? ?????? ??????
	 * 
	 * @param sandboxInstanceSequence
	 * @return
	 */
	public JSONObject allStop(Long sandboxInstanceSequence) throws Exception {
		logger.info("--- allStop --- sandboxInstanceSequence: " + sandboxInstanceSequence);
		JSONObject resultJson = new JSONObject();
		String key;

		List<Map<String, Object>> batchServices = batchMapper.batchServices(sandboxInstanceSequence);
		for (Map<String, Object> batchService : batchServices) {
			key = BATCH_KEY + batchService.get("BATCH_SERVICE_SEQUENCE_PK");
			if (MakeUtil.isNotNullAndEmpty(batchKeyAndFutureFunctionMap.get(key))) {
				logger.info("stopped job key :{}", key);
				batchKeyAndFutureFunctionMap.get(key).cancel(true);
				batchKeyAndExecutionCycleMap.remove(key);
				batchKeyAndFutureFunctionMap.remove(key);
				logger.info("--- allStop --- stop Complete key: " + key);
			} else {
				logger.info("--- allStop --- Not running job key: " + key);
			}
		}

		logger.info("--- allStop --- allStop Complete sandboxInstanceSequence: " + sandboxInstanceSequence);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * ?????? ?????? ??????
	 * 
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	public JSONObject batchStart(Long batchServiceSequencePk) throws NumberFormatException, Exception {
		logger.info("---- batchStart ????????? ?????? ( batchServiceSequencePk: " + batchServiceSequencePk + " )");

		JSONObject resultJson = new JSONObject();
		String batchKey, executionCycle;

		// ???????????? ?????????...??????
		batchStop(batchServiceSequencePk);

		Map<String, Object> batchServiceInfoMap = batchMapper.batchService(batchServiceSequencePk);

		if (batchServiceInfoMap != null && MakeUtil.isNotNullAndEmpty(batchServiceSequencePk)) {
			logger.info("A. Batch ?????? : " + batchServiceInfoMap.toString());
			batchKey = BATCH_KEY + batchServiceInfoMap.get("BATCH_SERVICE_SEQUENCE_PK");
			executionCycle = "0 " + batchServiceInfoMap.get("EXECUTION_CYCLE");

			if (batchKeyAndExecutionCycleMap.containsKey(batchKey)) {
				return resultJson;
			}

			batchKeyAndExecutionCycleMap.put(batchKey, executionCycle);
			logger.info("B. ????????? ???????????? (?????? ?????? ???????????? /list ??????)");
			logger.info("B.1 ????????? ?????? KEY ?????? :" + batchKey);
			logger.info("B.2 ????????? ?????? KEY ???????????? :" + executionCycle);
			ScheduledFuture<?> future = this.scheduler.schedule(() -> {
				try {
					logger.info("---- ???????????? ?????? ( batchServiceSequencePk: " + batchServiceSequencePk + " )");

					String applyDataPath = "" + batchServiceInfoMap.get("APPLY_DATA_PATH");
					String applyDataNameRule = "" + batchServiceInfoMap.get("APPLY_DATA_NAME_RULE");

					if (applyDataNameRule.contains("{")) {
						String applyDataRule = applyDataNameRule.substring(applyDataNameRule.indexOf("{") + 1, applyDataNameRule.indexOf("}")); // yyyyMMddHH
						SimpleDateFormat sdf = new SimpleDateFormat(applyDataRule);
						String applyDate = sdf.format(new Date());
						applyDataNameRule = applyDataNameRule.replace("{" + applyDataRule + "}", applyDate);
					}

					String applyDataFullPath = nfsResultPath + (applyDataPath.endsWith("/") ? applyDataPath : applyDataPath + "/") + applyDataNameRule;

					logger.info("A. ?????? ???????????? ?????? : " + applyDataFullPath);

					BatchLogService batchLogService = new BatchLogService();

					batchLogService.setBatchServiceSequence(batchServiceSequencePk);
					batchLogService.setBatchInstanceSequence((Long) batchServiceInfoMap.get("SANDBOX_INSTANCE_SEQUENCE_FK1"));

					doBatch(batchLogService, batchServiceInfoMap, batchServiceSequencePk, applyDataFullPath, executionCycle);

				} catch (Exception e) {
					logger.error("????????? ????????? ?????????????????????. (" + batchServiceSequencePk + ") : " + e.getMessage());
					e.printStackTrace();
				}
			},
					new CronTrigger(batchKeyAndExecutionCycleMap.get(batchKey)));
			batchKeyAndFutureFunctionMap.put(batchKey, future);
		} else {
			logger.error("????????? ??????????????? ????????????. (" + batchServiceSequencePk + ") ");

			resultJson.put("result", "fail");
			resultJson.put("type", "5000");
			return resultJson;
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * ?????? ?????? ??????
	 * 
	 * @param sandboxInstanceSequence
	 * @param batchServiceSequencePk
	 * @return
	 */
	public JSONObject batchStop(Long batchServiceSequencePk) throws Exception {
		logger.info("--- batchStop --- batchServiceSequencePk: " + batchServiceSequencePk);
		JSONObject resultJson = new JSONObject();
		String key;

		Map<String, Object> batchService = batchMapper.batchService(batchServiceSequencePk);
		if (MakeUtil.isNotNullAndEmpty(batchService)) {
			key = BATCH_KEY + batchService.get("BATCH_SERVICE_SEQUENCE_PK");
			if (MakeUtil.isNotNullAndEmpty(batchKeyAndFutureFunctionMap.get(key))) {
				logger.info("stopped job key :{}", key);
				batchKeyAndFutureFunctionMap.get(key).cancel(true);
				batchKeyAndExecutionCycleMap.remove(key);
				batchKeyAndFutureFunctionMap.remove(key);
				logger.info("--- batchStop --- stop Complete key: " + key);
			} else {
				logger.info("--- batchStop --- Not running job key: " + key);
			}
		} else {
			logger.error("????????? ??????????????? ????????????. (" + batchServiceSequencePk + ") ");

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
		String url = "http://" + instance.get("PRIVATE_IP") + ":" + modulePort + moduleMethod;

		if (MakeUtil.isNotNullAndEmpty(moduleTempUrl))
			url = moduleTempUrl + ":" + modulePort + moduleMethod;

		return url;
	}

	public JSONObject list() {
		logger.info("------ ?????? ???????????? ?????? cronMap --------------");

		for (String key : batchKeyAndExecutionCycleMap.keySet()) {
			logger.info("key : " + key);
			logger.info("ExecutionCycle : " + batchKeyAndExecutionCycleMap.get(key));

		}
		logger.info("------ ?????? ???????????? ?????? futureMap --------------");
		for (String key : batchKeyAndFutureFunctionMap.keySet()) {
			logger.info(key);
			logger.info("Function" + batchKeyAndFutureFunctionMap.get(key));
		}

		return new JSONObject();
	}

	public void doBatch(BatchLogService batchLogService, Map<String, Object> batchService, Long batchServiceSequencePk, String applyDataFullPath, String executionCycle) {
		LocalDateTime batchStartDatetime = LocalDateTime.now();

		String updateAttribute = "" + batchService.get("UPDATE_ATTRIBUTE");
		String resultUpdateMethod = "" + batchService.get("RESULT_UPDATE_METHOD");
		String totalColumnName = "" + batchService.get("TOTAL_COLUMN_NAME");
		String storeMethod = "" + batchService.get("STORE_METHOD");

		batchLogService.writeLogAboutBatchStart(batchStartDatetime, storeMethod, updateAttribute, resultUpdateMethod, executionCycle);
		batchStartDatetime = LocalDateTime.now();

		HttpService httpService = new HttpService();

		try {
			logger.info("B. ??????????????? ?????? ");

			String applyDataFullPathForModule = applyDataFullPath;
			logger.info("B.1. ????????? ???????????? ??????");
			logger.info("B.1.1. applyDataFullPathManager??? ?????? ?????? : " + applyDataFullPath);
			logger.info("B.1.1. applyDataFullPathForModule??? ?????? ?????? : " + applyDataFullPathForModule);

			
			// ????????? ????????? ????????? sql??? ?????? ???????????????????????? ???????????? ??????
			// if (batchService.get("MAKE_DATA_METHOD").toString().equals("sql")) {
			// 	// 1. sql ??? ???????????? ?????????
			// 	String sql = batchService.get("SQL").toString();
				
			// 	// 2. json ????????? ???????????? ??????
			// 	warehouseService.makeApplyFile(applyDataFullPath, sql);
			// }
			
			File applyDataFile = new File(applyDataFullPath);
			if (applyDataFile.exists()) {

				batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath, applyDataFile.length(), true, "");
				
				JSONArray applyDatasJson = new JSONArray();
				JSONObject httpJson = null;

				logger.info("B.2. ????????? ?????? ?????? ???????????? ????????? ??????");
				batchStartDatetime = LocalDateTime.now();

				// 2. ??????????????? ????????? ????????? ?????? ???????????? ????????? ?????????
				String moduleURL = getModuleURL(Long.parseLong("" + batchService.get("BATCH_INSTANCE_SEQUENCE_FK2")));
				if (batchServerDevTest) {
					moduleURL = batchServerUrl;
				}

				String batchCommandUrl = moduleURL + "/batchService/" + batchService.get("BATCH_SERVICE_SEQUENCE_PK");

				logger.info("B.2.1 ???????????? ????????? : " + batchCommandUrl);
				batchLogService.writeLogAboutBatchExecute(batchStartDatetime, true, "");

				batchStartDatetime = LocalDateTime.now();
				JSONObject param = new JSONObject();
				param.put("test_data_path", applyDataFullPathForModule);

				httpJson = httpService.httpServicePATCH(batchCommandUrl, param.toString());

				logger.info("B.2.2 ???????????? ????????? : " + httpJson.toString());

				// ?????????
				if ("200".equals(httpJson.get("type"))) {

					JSONObject data = JSONObject.fromObject(httpJson.get("data"));

					Scanner scan = new Scanner(applyDataFile);
					String content = null;

					while (scan.hasNextLine()) {
						content = scan.nextLine();
						applyDatasJson.add(JSONObject.fromObject(content));
					}
					scan.close();

					if (applyDatasJson.isEmpty()) {
						batchMapper.updateBatchServiceState(
								new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
						batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath,
								applyDataFile.length(), false, "????????? ?????? ???????????? ???????????? ????????????.");
						logger.error("????????? ?????? ???????????? ???????????? ????????????.(" + batchServiceSequencePk + ") : "
								+ applyDataFullPathForModule);
						return;
					}
					
					String domainIdColumnName = "" + batchService.get("DOMAIN_ID_COLUMN_NAME");
					JSONArray sendJsonArr;
					/// PostProceesing
					logger.info("B.3.1 ???????????? ?????? ");
					sendJsonArr = makePostProcessingArray(storeMethod, applyDatasJson, data, totalColumnName, domainIdColumnName);

					logger.info("B.4 index??? domainIdColumnName??? timeColumnName sorting");
					// ???????????? ??????
					sendJsonArr = MakeUtil.sortJsonArrWithTwoKey(sendJsonArr, domainIdColumnName, "predictedAt", "asc", "asc");

					logger.info("=========== " + batchServiceSequencePk + " batch process =========== sendJsonArr: " + sendJsonArr.toString());

					// ?????? ????????? ????????? HashMap ??????
					HashMap<String, JSONObject> resultMap = makeCoreStoreMap(sendJsonArr, domainIdColumnName, updateAttribute);

					batchLogService.writeLogAboutBatchExecuteResult(batchStartDatetime, true, "", httpJson.toString());
					batchStartDatetime = LocalDateTime.now();

					logger.info("B.5 ???????????? ??????????????? ?????? : " + resultUpdateMethod);
					String saveUrl = coreModuleIngestServerUrl + resultUpdateMethod;
					JSONObject entityFormat = new JSONObject();
					
					JSONArray paramsArray = new JSONArray();
					try {
						// ???????????? ??????????????? ??????
						for (String key : resultMap.keySet()) {
							JSONObject params = resultMap.get(key);

							JSONObject paramoneJsonObject = new JSONObject();
							paramoneJsonObject.put("id", key);
							paramoneJsonObject.put("type", batchService.get("RESULT_UPDATE_DOMAIN_ID"));
							paramoneJsonObject.put("predictions", params.get("predictions"));
							paramoneJsonObject.put("@context", 
									Arrays.asList(new String[] { "http://uri.citydatahub.kr/ngsi-ld/v1/context.jsonld", 
											"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld" }));
							paramsArray.add(paramoneJsonObject);
						}
						entityFormat.put("datasetId", batchService.get("DATASET_ID"));
						entityFormat.put("entities", paramsArray);

						httpJson = httpService.httpServicePOST(saveUrl, entityFormat);
						batchLogService.writeLogAboutBatchTransferResult(batchStartDatetime, true,"", entityFormat.toString().replaceAll("'\\{","{"), saveUrl);
						batchStartDatetime = LocalDateTime.now();
						logger.info("=========== " + batchServiceSequencePk + " batch process =========== httpJson: "
								+ httpJson.toString());

						batchMapper.updateBatchServiceState(
								new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_SUCCESS));

					} catch (Exception e) {
						batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
						logger.error(e.toString());
						batchLogService.writeLogAboutBatchTransferResult(batchStartDatetime, false, httpJson.toString(),
								entityFormat.toString().replaceAll("'\\{", "{"), saveUrl);
					}

				} else {
					batchMapper.updateBatchServiceState(
							new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
					batchLogService.writeLogAboutBatchExecuteResult(batchStartDatetime, false, httpJson.toString(), "");
					logger.info("=========== " + batchServiceSequencePk + " batch process =========== failed at batch module: ");
				}
			} else {
				batchMapper.updateBatchServiceState(
						new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
				batchLogService.writeLogAboutFileCheck(batchStartDatetime, applyDataFullPath, applyDataFile.length(),
						false, "?????? ????????? ?????? ????????? ???????????? ????????????.");
				logger.info("=========== " + batchServiceSequencePk + " batch process =========== Not Found fileFullName: " + applyDataFullPathForModule);
			}
		} catch (Exception e) {
			batchMapper.updateBatchServiceState(new BatchServiceState(batchServiceSequencePk, CODE_BATCH_SERVICE_FAIL));
			logger.error("=========== " + batchServiceSequencePk + " batch process ===========", e);
			logger.error(e.toString());
		}
		batchLogService.writeLogAboutBatchEnd(batchStartDatetime);
		return;

	}

	private HashMap<String, JSONObject> makeCoreStoreMap(JSONArray sendJsonArr, String domainIdColumnName, String updateAttribute) {
		HashMap<String, JSONObject> resultMap = new HashMap<String, JSONObject>();

		String targetEntityId = "";
		JSONArray tempJsonArray = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
		HashSet<String> updateAttributeMap = new HashSet<String>();

		for (String attribute : updateAttribute.split(",")) {
			updateAttributeMap.add(attribute);
		}

		for (int i = 0; i < sendJsonArr.size(); i++) {
			JSONObject sendJson = JSONObject.fromObject(sendJsonArr.get(i));
			JSONObject inputData = JSONObject.fromObject(sendJson.get("inputData").toString().replace("'", ""));
			
			if ("".equals(targetEntityId) || targetEntityId.equals(inputData.get(domainIdColumnName))) {
				targetEntityId = inputData.get(domainIdColumnName).toString();
				JSONObject tempJson = new JSONObject();
				for (Object key : sendJson.keySet()) {
					String keyStr = key.toString();
					if (updateAttributeMap.contains(keyStr)) {
						tempJson.put(keyStr, sendJson.get(keyStr));
					}
				}
				tempJsonArray.add(tempJson);
			} else {
				// resultMap??? ??????
				JSONObject congestionIndexPrediction = new JSONObject();
				JSONObject params = new JSONObject();
				congestionIndexPrediction.put("type", "Property");
				congestionIndexPrediction.put("value", tempJsonArray);
				if (updateAttributeMap.contains("observedAt")) {
					congestionIndexPrediction.put("observedAt", sdf.format(new Date()));
				}

				params.put("predictions", congestionIndexPrediction);
				resultMap.put(targetEntityId, params);

				targetEntityId = inputData.get(domainIdColumnName).toString();
				tempJsonArray = new JSONArray();
				JSONObject tempJson = new JSONObject();
				for (Object key : sendJson.keySet()) {
					String keyStr = key.toString();
					if (updateAttributeMap.contains(keyStr)) {
						tempJson.put(keyStr, sendJson.get(keyStr));
					}
				}
				tempJsonArray.add(tempJson);
			}

			if (i == (sendJsonArr.size() - 1)) {
				JSONObject congestionIndexPrediction = new JSONObject();
				JSONObject params = new JSONObject();
				congestionIndexPrediction.put("type", "Property");
				congestionIndexPrediction.put("value", tempJsonArray);
				if (updateAttributeMap.contains("observedAt")) {
					congestionIndexPrediction.put("observedAt", sdf.format(new Date()));
				}
				params.put("predictions", congestionIndexPrediction);
				resultMap.put(targetEntityId, params);
			}
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	private JSONArray makePostProcessingArray(String storeMethod, JSONArray applyDataJsonList,
			JSONObject predictResultJson, String totalColumnName, String domainIdColumnName)
			throws Exception {
		// ?????? ??????
		JSONArray sendJsonArr = new JSONArray();

		// ????????? ?????????????????? ?????? ???????????? ????????? ??????????????? ??????
		List<String> predictList = (List<String>) predictResultJson.get("predict");

		for (int i = 0; i < applyDataJsonList.size(); i++) {
			JSONObject sendJson = new JSONObject();
			JSONObject applyDataJson = JSONObject.fromObject(applyDataJsonList.get(i));

			// put inputData
			sendJson.put("inputData", "'" + applyDataJson.toString());

			// put predictedAt
			if (applyDataJson.get("predictedAt") != null) {
				sendJson.put("predictedAt", applyDataJson.get("predictedAt"));
			}

			// put inputData
			sendJson.put("predictValue", predictList.get(i));

			// put percentage
			if (storeMethod.equals("percentage")) {
				if (applyDataJson.get(totalColumnName) == null) {
					throw new Exception("Total ColumnName??? ???????????? ????????????. ");
				}
				
				float totalspotnumber = Float.parseFloat(applyDataJson.get(totalColumnName).toString());
				float predictFloat = Float.parseFloat(predictList.get(i));

				if (totalspotnumber != 0.0) {
					String percentage = String.format("%.2f", (totalspotnumber - predictFloat) / totalspotnumber * 100); // (totalspotnumber - ?????????) / totalspotnumber *100
					sendJson.put(storeMethod, percentage);
				} else {
					logger.info(applyDataJson.get(domainIdColumnName) + "??? totalspotnumber??? 0?????????.");
					sendJson.put(storeMethod, -1);
				}
			}
			sendJsonArr.add(sendJson);
		}
		return sendJsonArr;
	}
}
