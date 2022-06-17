package com.vaiv.analyticsManager.restFullApi.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.vaiv.analyticsManager.restFullApi.mapper.AlgorithmRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class AlgorithmRestService {
	
	@Autowired
	private AlgorithmRestMapper algorithmRestMapper;

	/**
	 * 알고리즘 조회
	 * @return
	 * @throws Exception
	 */
	
	public JSONObject algorithms() throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = algorithmRestMapper.algorithms();
		
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(JSONObject.fromObject(map));
		}
		result.put("result", "success");
		result.put("type", "2000");
		result.put("algorithms", jsonArr);
		return result;
	}

	/**
	 * 알고림즘 상세조회
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject algorithm(Integer id) throws Exception {
		JSONObject result = new JSONObject();
		
		Map<String, Object> detail = algorithmRestMapper.algorithm(id);
		if( MakeUtil.isNotNullAndEmpty(detail) )	result.put("algorithm", JSONObject.fromObject(detail));
		
		result.put("result", "success");
		result.put("type", "2000");
		return result;
	}

	/**
	 * 알고리즘 검색 조회
	 * @param value
	 * @return
	 */
	public JSONObject searchAlgorithms(String searchValue) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		List<Map<String, Object>> list = algorithmRestMapper.searchAlgorithms(searchValue);
		
		for (Map<String, Object> map : list) {
			if( MakeUtil.isNotNullAndEmpty(map) )	jsonArr.add(MakeUtil.nvlJson(JSONObject.fromObject(map)));
		}
		result.put("result", "success");
		result.put("type", "2000");
		result.put("algorithms", jsonArr);
		return result;
	}
	
	
}
