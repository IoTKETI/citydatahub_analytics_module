package com.vaiv;

import com.google.gson.JsonObject;
import com.vaiv.common.service.HttpService;
import com.vaiv.common.utils.MakeUtil;
import com.vaiv.restFull.service.BatchService;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


class AnalyticsBatchApplicationTests {
	private static Logger logger = LoggerFactory.getLogger(BatchService.class);

	@Test
	void basicBatchTest(){

		String batchServiceSequencePk="TEST";
		String nfsResultPath="/ANALYTICS_MANAGER_NFS/NIFI_RESULT";
		String coreModuleApiServerUrl="http://ip:port/entities/";

		String updateAttriubte="congestionIndexPrediction";
		String coreModuleApiServerMethod="/attrs/"+updateAttriubte;
		String resultUpdateMethod="update"; //replace or update

		String totalColumnName="totalspotnumber";
		Boolean isReverseIndex=true;
		String domainIdColumnName="parking_id";
		String storeMethod="index";


		HttpService httpService=new HttpService();

		try {
			logger.info("================================= NIFI에서 생성된 적용할 데이터 확인 =================================");

			String applyDataPath ="/data/test/"; // ""+batchService.get("APPLY_DATA_PATH");인
			String applyDataNameRule ="parkingstreet_select_result_for_twohours_apply_{yyyyMMddHH}.json"; //""+batchService.get("APPLY_DATA_NAME_RULE");
			String applyDataRule = applyDataNameRule.substring(applyDataNameRule.indexOf("{")+1,applyDataNameRule.indexOf("}")); // yyyyMMddHH

			SimpleDateFormat sdf = new SimpleDateFormat(applyDataRule);
			String applyDate = sdf.format(new Date());

			applyDataNameRule = applyDataNameRule.replace("{"+applyDataRule+"}", applyDate);

			String applyDataFullPathForManager ="/Users/hyeonwoo-mac/Desktop/MANAGER_CODE/analytics_batch_v2/AnalyticsBatch/parkingstreet_select_result_for_twohours_apply_2019121110.json";
			String applyDataFullPathForModule="/ANALYTICS_MANAGER_NFS/NIFI_RESULT/data/test/parkingstreet_select_result_for_twohours_apply_2019121110.json";//nfsResultPath + applyDataPath + applyDataNameRule;

			logger.info("applyDataFullPathManager 경로 : "+applyDataFullPathForManager);
			logger.info("applyDataFullPathForModule 경로 : "+applyDataFullPathForModule);

			File applyDataFile = new File(applyDataFullPathForManager);

			if( applyDataFile.exists() ) {

				JSONArray applyDatasJson = new JSONArray();
				JSONObject httpJson = null;


				logger.info("================================= 1. 적용할 파일 로드 =================================");
				// 1. 파일로부터 JSON Array를 읽음
				Scanner scan = new Scanner(applyDataFile);
				JSONObject contentJson = null;
				String content = null;
				while (scan.hasNextLine()) {
					content = scan.nextLine();
					applyDatasJson.add(contentJson.fromObject(content));
				}

				if (applyDatasJson.isEmpty()) {
					logger.error("=========== " + batchServiceSequencePk + " batch process =========== Their is no contents in File: " + applyDataFullPathForModule);
					return;
				}

				logger.info("================================= 2. 배치작업을 실행할 인스턴스 정보 로드  =================================");

				String moduleURL = "http://ip:port";
				String listUrl = moduleURL + "/modules/analyticsModule/batchService/" + "23";
				JSONObject param = new JSONObject();
				param.put("test_data_path", applyDataFullPathForModule);
				logger.info("================================= 3. 배치 적용 후 적용 값 가져옴  =================================");
				//3. 배치서버에서 배치를 실행한 결과값을 받음
				httpJson = httpService.httpServicePATCH(listUrl, param.toString());

				logger.info("=========== " + batchServiceSequencePk + " batch process =========== httpJson: " + httpJson.toString());

				logger.info("예측 결과값 : " + httpJson.toString());

				// 성공시
				JSONArray sendJsonArr = new JSONArray();

				if ("200".equals(httpJson.get("type"))) {
					// 결과 세팅
					long index = 0;
					double totalspotnumber = -1;
					float predict;


					String predictedAt = "";
					// 적용할 데이터에서의 값과 예측값을 가지고 인덱스값을 생성
					JSONObject data = new JSONObject().fromObject(httpJson.get("data"));

					List<String> predictList = (List<String>) data.get("predict");
					JSONObject applyDataJson = null;

					logger.info("================================= 4. index 값을 생성  =================================");
					JSONObject sendJson = null;
					for (int i = 0; i < applyDatasJson.size(); i++) {

						sendJson = new JSONObject();
						applyDataJson = JSONObject.fromObject(applyDatasJson.get(i));
						totalspotnumber = Double.parseDouble("" + applyDataJson.get(totalColumnName));
						predict = Float.parseFloat(predictList.get(i));
						index = 0;

						if (totalspotnumber == 0.0) {
							logger.info("totalsponumber가 0입니다.");
							continue;
						}
						predictedAt = applyDataJson.get("day") + "T" + applyDataJson.get("timeslot") + ":00:00,000+09:00"; // 예) 2019-06-14T11:00:00,000+09:00"
						if (isReverseIndex) {
							index = Math.round(((1.0f * totalspotnumber) - predict) / totalspotnumber * 100); // (totalspotnumber - 예측값) / totalspotnumber *100
						} else {
							index = Math.round((predict * 1.0f) / totalspotnumber * 100); // (totalspotnumber - 예측값) / totalspotnumber *100
						}

						sendJson.put(domainIdColumnName, applyDataJson.get(domainIdColumnName));
						sendJson.put("index", index);
						sendJson.put("predictedAt", predictedAt);

						sendJsonArr.add(sendJson);
					}

					logger.info("================================= 5. index를 domainIdColumnName으로  sorting  =================================");
					// sorting

					sendJsonArr = MakeUtil.sortJsonArr(sendJsonArr, domainIdColumnName, "asc");
					logger.info("=========== " + batchServiceSequencePk + " batch process =========== sendJsonArr: " + sendJsonArr.toString());

					// 결과 코어 모듈에 전송
					String parkingId = "";
					JSONObject params = null;
					JSONObject congestionIndexPrediction = null;

					JSONArray tempJsonArray = new JSONArray();
					JSONObject tempJson = new JSONObject();

					sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");

					//makeMapForResultStore
					HashMap<String, JSONObject> resultMap = new HashMap<String, JSONObject>();

					for (int i = 0; i < sendJsonArr.size(); i++) {
						sendJson = new JSONObject().fromObject(sendJsonArr.get(i));
						if ("".equals(parkingId) || parkingId.equals(sendJson.get(domainIdColumnName))) {

							parkingId = "" + sendJson.get(domainIdColumnName);
							tempJson = new JSONObject();
							tempJson.put("index", sendJson.get("index"));
							tempJson.put("predictedAt", sendJson.get("predictedAt"));
							tempJsonArray.add(tempJson);

						} else {
							//resultMap에 저장

							congestionIndexPrediction = new JSONObject();
							params = new JSONObject();
							congestionIndexPrediction.put("type", "Property");
							congestionIndexPrediction.put("value", tempJsonArray);
							congestionIndexPrediction.put("observedAt", sdf.format(new Date()));

							params.put("congestionIndexPrediction", congestionIndexPrediction);
							resultMap.put(parkingId, params);

							parkingId = "" + sendJson.get(domainIdColumnName);
							tempJsonArray = new JSONArray();
							tempJson = new JSONObject();
							tempJson.put("index", sendJson.get("index"));
							tempJson.put("predictedAt", sendJson.get("predictedAt"));
							tempJsonArray.add(tempJson);
						}

						if (i == (sendJsonArr.size() - 1)) {
							congestionIndexPrediction = new JSONObject();
							params = new JSONObject();
							congestionIndexPrediction.put("type", "Property");
							congestionIndexPrediction.put("value", tempJsonArray);
							congestionIndexPrediction.put("observedAt", sdf.format(new Date()));
							params.put("congestionIndexPrediction", congestionIndexPrediction);
							resultMap.put(parkingId, params);
						}
					}
					logger.info("================================= 6. 결과값을 코어모듈에 저장  =================================");
					//결과값을 코어모듈에 저장
					for (String key : resultMap.keySet()) {

						params=resultMap.get(key);
						String saveUrl = coreModuleApiServerUrl + key + coreModuleApiServerMethod;

						if(resultUpdateMethod.equals("update")){
							String getUrl=coreModuleApiServerUrl+key;
							params=getDataFromDataCoreAndUpdateNewlySettedParams(httpService, getUrl, params);
						}

						httpJson = httpService.httpServicePATCH(saveUrl, params.toString());
						logger.info("=========== " + batchServiceSequencePk + " batch process =========== httpJson: " + httpJson.toString());
					}

				} else {
					logger.info("=========== " + batchServiceSequencePk + " batch process =========== Not Found fileFullName: " + applyDataFullPathForModule);
				}
			}
		} catch (Exception e) {
			logger.error("=========== "+batchServiceSequencePk+" batch process ===========",e);
			e.printStackTrace();
		}
	}

	private JSONObject getDataFromDataCoreAndUpdateNewlySettedParams(HttpService httpService, String getUrl, JSONObject updateData) throws Exception {
		JSONObject getResult=httpService.httpServiceGET(getUrl);

		JSONObject domainInformation=(JSONObject)getResult.get("data");

		JSONArray storedValue=(JSONArray)((JSONObject)domainInformation.get("congestionIndexPrediction")).get("value");
		JSONArray newValue=(JSONArray)((JSONObject)updateData.get("congestionIndexPrediction")).get("value");

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

		((JSONObject)updateData.get("congestionIndexPrediction")).put("value", updateValue);


		return updateData;
	}

}
