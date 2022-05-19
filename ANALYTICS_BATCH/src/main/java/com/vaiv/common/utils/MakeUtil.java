package com.vaiv.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class MakeUtil {
	
	private static Logger logger = LoggerFactory.getLogger(MakeUtil.class);

	/**
	 * 객체를 GET방식의 파라미터로 변환
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public static String objToGetParam(Object obj) throws Exception{
		if( isNotNullAndEmpty(obj) ){
			StringBuffer result = new StringBuffer();
			String temp = "";
			Field field = null;
			int size = obj.getClass().getDeclaredFields().length;
			
			for (int i = 0; i < size; i++) {
				field = obj.getClass().getDeclaredFields()[i];
				field.setAccessible(true);
				String value = URLEncoder.encode(""+field.get(obj),"UTF-8");
				
				if( i < size-1 ){
					temp = field.getName() + "=" + value + "&";
				}else{
					temp = field.getName() + "=" + value;	
				}
				result.append(temp);
			}
			return result.toString();
		}
		return "";
	}
	
	/**
	 * Json의 value를 HTML TagFilter 변환
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public static JSONObject parsingToHtmlTagFilterOfJson(JSONObject json) throws Exception{
		JSONObject resultJson = new JSONObject();
		Iterator<?> keys = json.keys();

		while( keys.hasNext() ){
			String key = (String) keys.next();
			if( json.get(key) instanceof String ){
				String value = (String) json.get(key);
				value = parsingToHtmlTagFilter(value); // HTMLTagFilter 변환
				resultJson.put(key, value);
			}else{
				resultJson.put(key, json.get(key));
			}
		}	
		return resultJson;
	}
	
	/**
	 * HTMLTagFilter 변환
	 * @param value
	 * @return
	 */
	public static String parsingToHtmlTagFilter(String value) throws RuntimeException{
		String result = "";

		if( isNotNullAndEmpty(value) ){
			result = value.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&")
					      .replaceAll("&quot;", "\"").replaceAll("&apos;", "\'");
		}	
		return result;
	}
	
	/**
	 * Json "" TagFilter 변환
	 * @param value
	 * @return
	 * @throws RuntimeException
	 */
	public static String parsingToJsonTagFilter(String value) throws RuntimeException{
		String result = "";

		if( isNotNullAndEmpty(value) ){
			result = value.replaceAll("&quot;", "\"").replaceAll("amp;", "&");
		}	
		return result;
	}
	
	/**
	 * Null일경우 공백 반환
	 * @param str
	 * @param defaultValue
	 * @return
	 */
	public static String nvl(Object str){ return nvl(str, ""); }
	public static String nvl(Object str, String defaultValue){
		String val = "" ;
		
		if(str == null){
			val = defaultValue ;
		} else if("".equals(str.toString().trim())) {
			val = defaultValue ;
		} else if("null".equals(str.toString())) {
			val = defaultValue ;
		} else if("[null]".equals(str.toString())) {
			val = defaultValue ;
		} else if("undefined".equals(str.toString())) {
			val = defaultValue ;
		} else if("[]".equals(str.toString())) {
			val = defaultValue ;
		} else if("{}".equals(str.toString())) {
			val = defaultValue ;
		}else{
			val = str.toString().trim() ;
		}
		return val ;
	}
	
	/**
	 * Null 또는 공백 체크
	 * @param str
	 * @return
	 */
	public static boolean isNotNullAndEmpty(Object str){
		if(str == null){
			return false;
		} else if("".equals(str.toString().trim())) {
			return false;
		} else if("null".equals(str.toString())) {
			return false;
		} else if("[null]".equals(str.toString())) {
			return false;
		} else if("undefined".equals(str.toString())) {
			return false;
		} else if("[]".equals(str.toString())) {
			return false;
		} else if("{}".equals(str.toString())) {
			return false;
		}else{
			return true;			
		}
	}
	
	/**
	 * List 체크
	 * @param list
	 * @return
	 */
	public static boolean isNotNullAndEmpty(@SuppressWarnings("rawtypes") List list){
		if(list == null){
			return false;
		} else if(list.size() < 1) {
			return false;
		}else{
			return true;
		}
	}
	
	
	/**
	 * 숫자일 경우 true 반환
	 * @param value
	 * @return
	 */
	public static boolean isNumberCheck(String value){
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * UUID 추출
	 * @return
	 */
	public static String getUUID(){
		String result = "";
		UUID uuid = UUID.randomUUID();
		result = uuid+"";
		return result;
	}
	
	/**
	 * 클라이언트 IP 추출
	 * @param request
	 * @return
	 */
	public static String getClientIp(HttpServletRequest request){
		if( isNotNullAndEmpty(request) ){
			String clientIp = request.getHeader("X-FORWARDED-FOR");
			
			if( clientIp == null || clientIp.length() == 0 )	
				clientIp = request.getHeader("Proxy-Client-IP");
			
			if( clientIp == null || clientIp.length() == 0 )
				clientIp = request.getHeader("WL-Proxy-Client-IP");
			
			if( clientIp == null || clientIp.length() == 0 )
				clientIp = request.getRemoteAddr();
			
			return clientIp;	
		}else{
			return null;
		}
	}
	
	/**
	 * Excepttion Logger 찍기
	 * @param e
	 * @param name
	 * @return
	 */
	public static String printErrorLogger(Throwable e, String name){
		String log = makeStackTrace(e);
		logger.error("Error "+name+" : "+log);
		return log;
	}
	
	/**
	 * Exception 로그 String으로 변환
	 * @param t
	 * @return
	 */
	public static String makeStackTrace(Throwable t){
		if( t == null )	return "";
		
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bout));
			bout.flush();
			
			return new String(bout.toByteArray());
			
		} catch (Exception e) {
			return "";
		}
	}
	
	
	/**
	 * JSONArray json print
	 * @param jsonArr
	 * @param log
	 * @throws Exception
	 */
	public static void printForJsonArr(JSONArray jsonArr, boolean log) throws Exception{
		JSONObject json = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		for (int i = 0; i < jsonArr.size(); i++) {
			json = JSONObject.fromObject(jsonArr.get(i));
			
			if( log ) logger.info(i+" "+gson.toJson(json));
			else System.out.println(i+" "+gson.toJson(json));
		}
	}
	
	/**
	 * JsonArray key값으로 sorting
	 * @param jsonArr
	 * @param key
	 * @param priority
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static JSONArray sortJsonArr(JSONArray jsonArr, final String key, final String priority) throws RuntimeException{
		// sorting
		Comparator<JSONObject> comparatorJson = new Comparator<JSONObject>(){
			public int compare(final JSONObject o1, final JSONObject o2 ){
				
				String c1 = MakeUtil.nvl(o1.get(key));
				String c2 = MakeUtil.nvl(o2.get(key));
				
				if( "desc".equals(priority) ){
					return c2.compareTo(c1);
				}else{
					return c1.compareTo(c2);	
				}
			}
		};
		
		Collections.sort(jsonArr, comparatorJson);
		return jsonArr;
	}
	
	/**
	 * JsonArray key값으로 sorting
	 * @param jsonArr
	 * @param key
	 * @param priority
	 * @return
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	public static JSONArray sortJsonArrWithTwoKey(JSONArray jsonArr, final String key, final String key2, final String priority, final String priority2) throws RuntimeException{
		// sorting
		Comparator<JSONObject> comparatorJson = new Comparator<JSONObject>(){
			public int compare(final JSONObject o1, final JSONObject o2 ){

				JSONObject b1=JSONObject.fromObject(o1.get("inputData").toString().replace("'",""));
				JSONObject b2=JSONObject.fromObject(o2.get("inputData").toString().replace("'",""));

				String c1 = MakeUtil.nvl(b1.get(key));
				String c2 = MakeUtil.nvl(b2.get(key));

				String d1 = MakeUtil.nvl(o1.get(key2));
				String d2 = MakeUtil.nvl(o2.get(key2));

				if(c1.equals(c2)){
					if( "desc".equals(priority2) ){
						return d2.compareTo(d1);
					}else{
						return d1.compareTo(d2);
					}
				}

				if( "desc".equals(priority) ){
					return c2.compareTo(c1);
				}else{
					return c1.compareTo(c2);
				}
			}
		};

		Collections.sort(jsonArr, comparatorJson);
		return jsonArr;
	}
	
	/**
	 * Object의 value값이 Null일경우 공백 반환
	 * @param obj
	 * @return
	 */
	public static Object nvlObj(Object obj){
		if( MakeUtil.isNotNullAndEmpty(obj) && obj instanceof JSONObject ){
			obj = nvlJson((JSONObject)obj);
			
		}else if( MakeUtil.isNotNullAndEmpty(obj) && obj instanceof JSONArray ){
			obj = nvlJsonArray((JSONArray)obj);
			
		}else{
			logger.info("nvlObj instanceof "+obj.getClass().getName());
		}
		return obj;
	}
	
	
	/**
	 * JSONObject의 value값이 Null일경우 공백 반환
	 * @param jsonObj
	 * @return
	 */
	public static JSONObject nvlJson(JSONObject jsonObj){
		if( MakeUtil.isNotNullAndEmpty(jsonObj) && jsonObj instanceof JSONObject ){
			
			try {
				String key;
				Iterator<?> it = jsonObj.keys();
				
				while( it.hasNext() ){
					key = (String)it.next();

					if( MakeUtil.isNotNullAndEmpty(jsonObj.get(key)) && jsonObj.get(key) instanceof JSONArray ){
						jsonObj.put(key, nvlJsonArray((JSONArray)jsonObj.get(key)));
						
					}else if( MakeUtil.isNotNullAndEmpty(jsonObj.get(key)) && jsonObj.get(key) instanceof JSONObject ){
						jsonObj.put(key, nvlJson((JSONObject)jsonObj.get(key)));
						
					}else{
						jsonObj.put(key, nvl(jsonObj.get(key)));
					}
				}
			} catch (Exception e) {
				printErrorLogger(e, "nvlJson");
			}
		}
		return jsonObj;
	}
	
	/**
	 * JSONArray의 value값이 Null일경우 공백 반환
	 * @param jsonArr
	 * @return
	 */
	public static JSONArray nvlJsonArray(JSONArray jsonArr){
		JSONArray moveJsonArr = new JSONArray();
		if( MakeUtil.isNotNullAndEmpty(jsonArr) && jsonArr instanceof JSONArray ){

			try {
				for (int i = 0; i < jsonArr.size(); i++) {
					
					if( MakeUtil.isNotNullAndEmpty(jsonArr.get(i)) && jsonArr.get(i) instanceof JSONArray ){
						moveJsonArr.add(nvlJsonArray((JSONArray)jsonArr.get(i)));
						
					}else if( MakeUtil.isNotNullAndEmpty(jsonArr.get(i)) && jsonArr.get(i) instanceof JSONObject ){
						moveJsonArr.add(nvlJson(JSONObject.fromObject(jsonArr.get(i))));
						
					}else{
						moveJsonArr.add(jsonArr.get(i));
					}
					
				}
			} catch (Exception e) {
				printErrorLogger(e, "nvlJsonArray");
			}
		}
		return moveJsonArr;
	}
	
	/**
	 * JSONArray에서 value값 중복 체크
	 * @param jsonArr
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean chkDuplicationKey(JSONArray jsonArr, String key, String value) throws Exception {
		if( isNotNullAndEmpty(key) && isNotNullAndEmpty(value) ){
			JSONObject json = null;
			for (int i = 0; i < jsonArr.size(); i++) {
				json = JSONObject.fromObject(jsonArr.get(i));
				if( value.equals(json.get(key)+"") )
					return true;
			}	
		}
		return false;
	}
	
	/**
	 * 구분자(separator)에 맞게 String을 List로 담기
	 * separator default는 ,
	 * @param param
	 * @return
	 */
	public static List<String> parsingStringToList(String param) throws Exception {
		return parsingStringToList(param, ",");
	}
	/**
	 * 구분자(separator)에 맞게 String을 List로 담기
	 * @param param
	 * @param separator
	 * @return
	 */
	public static List<String> parsingStringToList(String param, String separator) throws Exception {
		List<String> list = new ArrayList<String>();
		if( isNotNullAndEmpty(param) ){
			if( param.contains(separator) ){
				list = Arrays.asList(param.split(separator));
			}else{
				list.add(param);
			}
		}
		return list;
	}
	
	/**
	 * 날짜 차이 비교
	 * @param begin
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public static long diffOfDate(String begin, String end) throws Exception {
		return diffOfDate(begin, end, "date");
	}
	
	/**
	 * 날짜 / 시간 / 분 / 초 차이 비교
	 * @param begin
	 * @param end
	 * @param option : date, time, minute, second
	 * @return
	 * @throws Exception
	 */
	public static long diffOfDate(String begin, String end, String option) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long diffDays = 0;
		
		Date beginDate = formatter.parse(begin);
		Date endDate = formatter.parse(end);

		long diff = endDate.getTime() - beginDate.getTime();
		
		/* long diffDays = diff / (시 * 분 * 초 * milisecond); */
		if( "time".equals(option) ) {
			diffDays = diff / (60 * 60 * 1000);
		}else if( "minute".equals(option) ) {
			diffDays = diff / (60 * 1000);
		}else if( "second".equals(option) ) {
			diffDays = diff / (1000);	
		}else{
			diffDays = diff / (24 * 60 * 60 * 1000);
		}

		return diffDays;
	}
	
	/**
	 * 시간 : 분 : 초 차이값 봔환
	 * @param begin
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public static String diffOfDateAll(String begin, String end) throws Exception{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String diffResult = "";
		long diffTime = 0;
		long diffMinute = 0;
		long diffSecond = 0;
		
		Date beginDate = formatter.parse(begin);
		Date endDate = formatter.parse(end);

		long diff = endDate.getTime() - beginDate.getTime();
		
		/* long diffDays = diff / (시 * 분 * 초 * milisecond); */
		diffTime = diff / (60 * 60 * 1000); 				// 시간차이
		diffMinute = diff / (60 * 1000) - (60*diffTime);	// 분 차이
		diffSecond = diff / (1000) - (60*diffMinute);		// 초 차이
		
		diffResult += (diffTime>9?diffTime:"0"+diffTime) + ":";
		diffResult += (diffMinute>9 ? diffMinute : "0"+diffMinute) + ":";
		diffResult += diffSecond>9 ? diffSecond : "0"+diffSecond;
		
		return diffResult;
	}
	
	
}
