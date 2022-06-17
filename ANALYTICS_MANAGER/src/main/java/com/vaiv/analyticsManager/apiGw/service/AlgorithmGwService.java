package com.vaiv.analyticsManager.apiGw.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.apiGw.mapper.AlgorithmGwMapper;
import com.vaiv.analyticsManager.common.service.RestFullReturnService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class AlgorithmGwService {
	
	@Autowired
	private AlgorithmGwMapper algorithmGwMapper;
	
	@Autowired
	private RestFullReturnService restFullReturnService;

	/**
	 * 알고리즘 목록 조회 API
	 * @return
	 */
	public JSONArray algorithmsGw() throws Exception {
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = algorithmGwMapper.algorithmsGw();
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(JSONObject.fromObject(map));
		}
		
		return jsonArr;
	}
	
	/**
	 * 알고림즘 상세조회 API
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject algorithmGw(String algorithmPk) throws Exception {
		JSONObject result = new JSONObject();
		int algorithmId;
		try {
			algorithmId = Integer.parseInt(algorithmPk);
		} catch (Exception e) {
			return restFullReturnService.badRequestData("parameter type Error");
		}
		
		Map<String, Object> detail = algorithmGwMapper.algorithmGw(algorithmId);
		if( detail == null ) {
			return restFullReturnService.resourceNotFound("Not found Algorithm");
		}
		result = MakeUtil.nvlJson(JSONObject.fromObject(detail));
		return result;
	}
	
}
