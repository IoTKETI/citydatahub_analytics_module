package com.vaiv.analyticsManager.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vaiv.analyticsManager.common.service.AuthService;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MainController {

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
	@RequestMapping("/")
	public void rootPath(@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "state", required = false) String state, HttpServletResponse response,
			HttpServletRequest request) {
		try {
			response.sendRedirect("/admin/main");
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

}
