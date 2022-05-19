package com.vaiv.analyticsManager.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vaiv.analyticsManager.common.service.AuthService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AdminController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthService authService;

	/**
	 * 통합모듈에서 로그인 후 code, state값 받음(interceptor에서 처리)
	 * 
	 * @param code
	 * @param state
	 * @param response
	 * @param request
	 */
	@RequestMapping("/admin/main")
	public void main(@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response,
			HttpServletRequest request) {
		try {
			response.sendRedirect("/admin/algorithmManage");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 로그아웃
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@GetMapping("/admin/logout")
	public RedirectView logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		logger.info("logout");
		String requestUrl = "" + request.getRequestURL();
		String message = authService.logout("" + session.getAttribute("userId"), request, requestUrl);
		logger.info("logout message: " + message.toString());
		if (MakeUtil.isNotNullAndEmpty(message)) {
			JsonObject json = new JsonParser().parse(message).getAsJsonObject();
			if ("success".equals(json.get("result").getAsString())
					|| "session does not exist".equals(json.get("description").getAsString())) {

				authService.removeCookie(request, response, requestUrl);
				authService.removeSession(request);
			}
		}

		return new RedirectView("/admin/algorithmManage");
	}

	/**
	 * 알고리즘 조회
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/admin/algorithmManage")
	public String algorithm(HttpServletRequest request, HttpSession session) {
		return "admin/algorithm/algorithmManageAdmin";
	}

	/**
	 * 프로젝트 관리
	 * 
	 * @return
	 */
	@GetMapping("/admin/projectManage")
	public String project(HttpSession session) {
		return "admin/project/projectManageAdmin";
	}

	/**
	 * 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@PostMapping("/admin/projectDetail")
	public String projectDetail(@RequestParam(value = "projectSequencePk") String projectSequencePk, Model model,
			HttpSession session) {
		model.addAttribute("projectSequencePk", projectSequencePk);
		return "admin/project/projectDetailAdmin";
	}

	/**
	 * 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping("/admin/projectDetail")
	public String projectDetailGet(@RequestParam(value = "id") String id, Model model, HttpSession session) {
		model.addAttribute("projectSequencePk", id);
		return "admin/project/projectDetailAdmin";
	}

	/**
	 * 배치 관리
	 * 
	 * @return
	 */
	@GetMapping("/admin/batchManage")
	public String batch(HttpSession session) {
		return "admin/batch/batchManageAdmin";
	}
}
