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
public class MainController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${sso.username}")
	private String ssoUsername;

	@Value("${sso.userId}")
	private String ssoUserId;

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
	@RequestMapping("/")
	public void rootPath(@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response,
			HttpServletRequest request) {
		try {

			String userRole = (String) request.getSession().getAttribute("userRole");

			if (userRole.equals("Analytics_Admin"))
				response.sendRedirect("/admin/main");
			else
				response.sendRedirect("/algorithmManage");

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
	@GetMapping("/logout")
	public RedirectView logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		logger.info("logout");
		String requestUrl = "" + request.getRequestURL();
		String userId = "" + session.getAttribute("userId");
		if (ssoUsername.equals(userId))
			userId = ssoUserId;

		String message = authService.logout(userId, request, requestUrl);
		logger.info("logout message: " + message.toString());
		if (MakeUtil.isNotNullAndEmpty(message)) {
			JsonObject json = new JsonParser().parse(message).getAsJsonObject();
			if ("success".equals(json.get("result").getAsString())
					|| "session does not exist".equals(json.get("description").getAsString())
					|| "unauthorized".equals(json.get("description").getAsString())) {

				authService.removeCookie(request, response, requestUrl);
				authService.removeSession(request);
			}
		}

		return new RedirectView("/algorithmManage");
	}

	/**
	 * 알고리즘 조회
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/algorithmManage")
	public String algorithm() {
		return "user/algorithm/algorithmManage";
	}

	/**
	 * 샌드박스 관리
	 * 
	 * @return
	 * @throws SchedulerException
	 */
	@GetMapping("/sandboxManage")
	public String sandboxManage() {
		return "user/sandbox/sandboxManage";
	}

	/**
	 * 프로젝트 관리
	 * 
	 * @return
	 */
	@GetMapping("/projectManage")
	public String project() {
		return "user/project/projectManage";
	}

	/**
	 * 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@PostMapping("/projectDetail")
	public String projectDetail(@RequestParam(value = "projectSequencePk") String projectSequencePk, Model model,
			HttpSession session) {
		model.addAttribute("projectSequencePk", projectSequencePk);
		return "user/project/projectDetail";
	}

	/**
	 * 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping("/projectDetail")
	public String projectDetailGet(@RequestParam(value = "id") String id, Model model, HttpSession session) {

		model.addAttribute("projectSequencePk", id);

		return "user/project/projectDetail";
	}

	/**
	 * 배치 관리
	 * 
	 * @return
	 */
	@GetMapping("/batchManage")
	public String batch() {
		return "user/batch/batchManageUser";
	}
}
