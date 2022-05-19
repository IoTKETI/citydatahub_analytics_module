package com.vaiv.common.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import com.vaiv.common.utils.FileUtil;
import com.vaiv.common.utils.MakeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@Component
public class HttpService {

	private Logger logger = LoggerFactory.getLogger(HttpService.class);
	private OkHttpClient client;
	
	public HttpService() {
		try {
			
			client = new OkHttpClient();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.connectTimeout(30, TimeUnit.SECONDS); 
            builder.readTimeout(30, TimeUnit.SECONDS); 
            builder.writeTimeout(30, TimeUnit.SECONDS); 
            client = builder.build();
            
            logger.info("--- HttpService : Set client ");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("--- HttpService : "+e.toString());
		}
	}

	/**
	 * HttpService GET 
	 * @param connUrl
	 * @return
	 * @throws IOException
	 */
	public JSONObject httpServiceGET(String connUrl) throws Exception{
		logger.info("--- httpServiceGET connUrl: "+connUrl+" ---");
		JSONObject result = new JSONObject();
		Request request = null;
		
		request = new Request.Builder().header("Accept", "application/ld+json").url(connUrl).get().build();
		
		String resMessage = "";
		
		Response response = client.newCall(request).execute();
		resMessage = response.body().string();
		

		result.put("type", ""+response.code());
		result.put("title", response.message());
		result.put("data", resMessage);
		
		response.body().close();
		logger.info("--- httpServiceGET result : "+result.toString());
		return MakeUtil.nvlJson(result);
	}
	
	/**
	 * httpService POST
	 * @param connUrl
	 * @param jsonMessage
	 * @return
	 * @throws IOException
	 */
	public JSONObject httpServicePOST(String connUrl, JSONObject jsonObject) throws Exception{
		logger.info("--- httpServicePOST connUrl: "+connUrl+", jsonMessage: "+jsonObject.toString()+" ---");
		JSONObject result = new JSONObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";
		
		RequestBody requestBody = RequestBody.create(null, jsonObject.toString());
		okRequest = new Request.Builder().addHeader("Accept", "application/json")
				.addHeader("Content-Type", "application/json").url(connUrl).post(requestBody).build();
		
		response = client.newCall(okRequest).execute();
		resMessage = response.body().string();
		
		result.put("type", ""+response.code());
		result.put("title", response.message());
		result.put("data", resMessage);
		
		response.body().close();
		logger.info("--- httpServicePOST result : "+result.toString());
		return MakeUtil.nvlJson(result);
		
	}
	
	/**
	 * httpService PATCH
	 * @param connUrl
	 * @param jsonMessage
	 * @param option
	 * @return
	 * @throws Exception
	 */
	public JSONObject httpServicePATCH(String connUrl, String jsonMessage) throws Exception{
		logger.info("--- httpServicePATCH connUrl: "+connUrl+", jsonMessage: "+jsonMessage+" ---");
		JSONObject result = new JSONObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";
		
		RequestBody requestBody = RequestBody.create(MediaType.parse("raw"),jsonMessage.toString());
		okRequest = new Request.Builder()
				.addHeader("Accept", "application/json")
				.addHeader("Accept-Charset", "UTF-8")
				.addHeader("Content-Type", "application/json")
				.url(connUrl).patch(requestBody).build();

		logger.info("--- httpServicePATCH okRequest.headers : "+okRequest.headers().toString());
		response = client.newCall(okRequest).execute();
		resMessage = response.body().string();
		
		result.put("type", ""+response.code());
		result.put("title", response.message());
		result.put("data", resMessage);
		
		response.body().close();
		logger.info("--- httpServicePATCH result : "+result.toString());
		return MakeUtil.nvlJson(result);
		
	}
	
	//@Test
	public void httpServicePATCH2() {
		OkHttpClient client4;
		client4 = new OkHttpClient();
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30, TimeUnit.SECONDS); 
        builder.readTimeout(30, TimeUnit.SECONDS); 
        builder.writeTimeout(30, TimeUnit.SECONDS); 
        client4 = builder.build();
		String connUrl="http://ip:port/entities/urn:datahub:OffStreetParking:yt_lot_3/attrs/congestionIndexPrediction";
		String jsonMessage="{\"congestionIndexPrediction\":{\"type\":\"Property\",\"value\":[{\"index\":73,\"predictedAt\":\"2019-12-10T14:00:00,000+09:00\"},{\"index\":74,\"predictedAt\":\"2019-12-10T15:00:00,000+09:00\"},{\"index\":74,\"predictedAt\":\"2019-12-10T16:00:00,000+09:00\"}],\"observedAt\":\"2019-12-12T10:56:30,291+09:00\"}}";
		logger.info("--- httpServicePATCH connUrl: "+connUrl+", jsonMessage: "+jsonMessage+" ---");
		JSONObject result = new JSONObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";
		
		RequestBody requestBody = RequestBody.create(MediaType.parse("raw"),jsonMessage.toString());
		okRequest = new Request.Builder()
				.addHeader("Accept", "application/json")
				.addHeader("Accept-Charset", "UTF-8")
				.addHeader("Content-Type", "application/json")
				.url(connUrl).patch(requestBody).build();

		logger.info("--- httpServicePATCH okRequest.headers : "+okRequest.headers().toString());
		logger.info("--- data : " + okRequest.body().contentType());
		logger.info("--- jsonMessage : " + jsonMessage.toString());
		logger.info("--- data : " + okRequest.body());
		
		try {
			response = client4.newCall(okRequest).execute();
			resMessage = response.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		result.put("type", ""+response.code());
		result.put("title", response.message());
		result.put("data", resMessage);
		
		response.body().close();
		logger.info("--- httpServicePATCH result : "+result.toString());
			
	}
	
	
	/**
	 * httpService DELETE
	 * @param connUrl
	 * @return
	 * @throws Exception
	 */
	public JSONObject httpServiceDELETE(String connUrl) throws Exception{
		logger.info("--- httpServiceDELETE connUrl: "+connUrl+" ---");
		JSONObject result = new JSONObject();
		Request request = null;
		
		request = new Request.Builder().url(connUrl).delete().build();
		String resMessage = "";
		
		Response response = client.newCall(request).execute();
		resMessage = response.body().string();
		
		result.put("type", ""+response.code());
		result.put("title", response.message());
		result.put("data", resMessage);
		
		response.body().close();
		logger.info("--- httpServiceDELETE result : "+result.toString());
		return MakeUtil.nvlJson(result);
	}
	
	
	
	/**
	 * 파일 다운로드
	 * @param connUrl
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public void httpServiceDownloader(String connUrl, String filePath, String fileName) throws Exception{
		logger.info("--- httpServiceDownloader connUrl: "+connUrl+", filePath: "+filePath+", fileName: "+fileName+" ---");
        FileOutputStream fos = null;
        InputStream is = null;
        try {
        	FileUtil.mkdir(filePath);
            fos = new FileOutputStream(filePath + "\\" + fileName);
 
            URL url = new URL(connUrl);
            URLConnection urlConnection = url.openConnection();
            is = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = is.read(buffer)) != -1) {
                fos.write(buffer, 0, readBytes);
            }
        } finally {
            if (fos != null)	fos.close();
            if (is != null)		is.close();
        }
        logger.info("--- httpServiceDownloader compleate download!! ---");
	}
	
}
