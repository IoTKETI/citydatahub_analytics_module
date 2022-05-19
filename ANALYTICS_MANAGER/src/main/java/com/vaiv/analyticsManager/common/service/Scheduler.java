package com.vaiv.analyticsManager.common.service;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.vaiv.analyticsManager.restFullApi.service.SandboxRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.domain.Instance;
import com.vaiv.analyticsManager.restFullApi.mapper.SandboxRestMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.sf.json.JSONObject;

@Component
public class Scheduler {
	
	private static Logger logger = LoggerFactory.getLogger(Scheduler.class);
	
	@Autowired
	private SandboxRestMapper sandboxRestMapper;

	@Autowired
	private SandboxRestService sandboxRestService;
	
	@Autowired
	private HttpService httpService;

	@Value("${module.tempUrl}")
	private String moduleTempUrl;
	
	@Value("${module.port}")
	private String modulePort;
	
	@Value("${module.method}")
	private String moduleMethod;
	
	@Value("${module.healthCheck}")
	private String healthCheck;


	
	/**
	 * 30초마다 server state가 call인  체크 후 변경
	 */
	// @SuppressWarnings("static-access")
	//  @Scheduled(fixedDelay = 30000) 
	// public void instanceSynchronization() {
	// 	logger.info("####### instanceSynchronization Start #######");
	// 	try {
	// 		sandboxRestService.checkServerStateFromCloudApi();
	// 		logger.info("####### instanceSynchronization End #######");
	// 		return ;

	// 	} catch (Exception e) {
	// 		logger.error("####### instanceSynchronization Error : ",e);
	// 		e.printStackTrace();
	// 		return ;
	// 	}
	// }
	
	/**
	 * 인스턴스 모듈상태 헬스 체크
	*/
	// @Scheduled(fixedDelay = 30000)
	// public void instanceHealthCheck() {
	// 	logger.info("$$$$$ instanceHealthCheck Start $$$$$");
	// 	String url = null;
	// 	String moduleState = null;
	// 	JSONObject jsonResult = null;
	// 	String serverState = "start_done";
	// 	Instance instance = null;
	// 	int instanceSequencePk = 0;
		
	// 	try {
	// 		// 인스턴스 목록 조회(시작인것들만 가져와서)
	// 		List<Map<String, Object>> instanceList = sandboxRestMapper.InstancesOfServerState(serverState);

	// 		if( instanceList.size() > 0 ) {
	// 			for( Map<String, Object> instanceMap : instanceList ) {
	// 				try {
	// 					instanceSequencePk = Integer.parseInt(""+instanceMap.get("INSTANCE_SEQUENCE_PK"));

	// 					logger.info("instance Map : " + instanceMap.toString());
	// 					logger.info("private ip : " + instanceMap.get("PRIVATE_IP"));
	// 					logger.info("module temp url : " + moduleTempUrl);

	// 					if( MakeUtil.isNotNullAndEmpty(instanceMap.get("PRIVATE_IP")) ) {
	// 						url = "http://"+instanceMap.get("PRIVATE_IP") + ":" + modulePort + moduleMethod + healthCheck;
							
	// 						if( MakeUtil.isNotNullAndEmpty(moduleTempUrl) )
	// 							url = moduleTempUrl + ":" + modulePort + moduleMethod + healthCheck;

	// 						logger.info("url : " + url);

	// 						/* 인스턴스 상태 값 변경
	// 						 * 모듈 상태값
	// 						 * 정상: success 상태이상: fail 분석모듈 서버 죽음: server_die 분석모듈 종료: server_end
	// 						 */
	// 						try {
	// 							jsonResult = httpService.httpServiceGET(url, "");
	// 							if( "200".equals(jsonResult.get("type")) ) {
	// 								moduleState = "success";
									
	// 								// computer_clock 값이 현시간과 3분 차이나면 ==> 상태이상
	// 								JsonParser parser = new JsonParser();
	// 								JsonObject json = parser.parse(""+jsonResult.get("data")).getAsJsonObject();
	// 								json = parser.parse(""+json.get("HEALTH_INFO")).getAsJsonObject();
	// 								String computerClock = json.get("computer_clock").getAsString().substring(0,19);
	// 								String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

	// 								if( MakeUtil.diffOfDate(computerClock, currentDate, "minute") > 3 ) {
	// 									logger.info("모듈업데이트된 시간:"+computerClock+" vs "+"현재서버시간 : "+currentDate);
	// 									moduleState = "fail";
	// 									logger.info("$$$$$ instanceHealthCheck fail With Time Limit, INSTANCE_SEQUENCE_PK: "+instanceMap.get("INSTANCE_SEQUENCE_PK")+" $$$$$");
	// 								}
									
	// 							}else {
	// 								moduleState = "server_end";
	// 								logger.info("$$$$$ instanceHealthCheck type: "+jsonResult.get("type")+", title: "+jsonResult.get("title")+" $$$$$");
	// 								logger.info("$$$$$ instanceHealthCheck moduleState: server_end, INSTANCE_SEQUENCE_PK: "+instanceMap.get("INSTANCE_SEQUENCE_PK")+" $$$$$");
	// 							}
								
	// 						} catch (ConnectException e) {
	// 							moduleState = "server_die";
	// 							logger.info("$$$$$ instanceHealthCheck ConnectException e: "+e+" $$$$$");
	// 							logger.info("$$$$$ instanceHealthCheck moduleState: server_die, INSTANCE_SEQUENCE_PK: "+instanceMap.get("INSTANCE_SEQUENCE_PK")+" $$$$$");
	// 						}
							
	// 						if( !moduleState.equals(""+instanceMap.get("MODULE_STATE")) ){
	// 							instance = new Instance();
	// 							instance.setInstanceSequencePk(instanceSequencePk);
	// 							instance.setModuleState(moduleState);
	// 							sandboxRestMapper.updateInstance(instance);
	// 							logger.info("$$$$$ instanceHealthCheck updateInstance moduleState: "+moduleState+", INSTANCE_SEQUENCE_PK: "+instanceMap.get("INSTANCE_SEQUENCE_PK")+" $$$$$");
								
	// 						}else {
	// 							logger.info("$$$$$ instanceHealthCheck instance have nothing to change moduleState... $$$$$");
	// 						}
	// 					}
						
	// 				} catch (Exception e) {
	// 					if( e.toString().equals("java.net.SocketTimeoutException: connect timed out")) {
	// 						try {
	// 							instance = new Instance();
	// 							instance.setInstanceSequencePk(instanceSequencePk);
	// 							instance.setModuleState("server_die");
	// 							sandboxRestMapper.updateInstance(instance);
	// 						} catch (Exception e1) {
	// 							e1.printStackTrace();
	// 						}
	// 						logger.info("$$$$$ instanceHealthCheck updateInstance moduleState: \"server_die\", INSTANCE_SEQUENCE_PK: "+instanceSequencePk+" $$$$$");
	// 					}
	// 					logger.error("$$$$$ instanceHealthCheck Error : ",e);
	// 					e.printStackTrace();
	// 				}
	// 			}
				
	// 		}else {
	// 			logger.info("$$$$$ instanceHealthCheck instance have nothing... $$$$$");
	// 		}
	// 	} catch (Exception e) {
	// 		logger.error("$$$$$ instanceHealthCheck Error : ",e);
	// 		e.printStackTrace();
	// 	}
	// 	logger.info("$$$$$ instanceHealthCheck End $$$$$");
	// }
}
