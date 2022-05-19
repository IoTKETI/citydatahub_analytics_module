package com.vaiv.analyticsManager.common.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vaiv.analyticsManager.common.utils.EncryptionUtil;
import com.vaiv.analyticsManager.common.utils.MakeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import net.sf.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class AuthService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${sso.dataHubUrl_pub_admin}")
	private String dataHubUrlPubAdmin;
	// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
	@Value("${sso.dataHubUrl_pri_admin}")
	private String dataHubUrlPriAdmin;

	@Value("${sso.authEndpoint}")
	private String authEndpoint;
	@Value("${sso.responseType}")
	private String responseType;

	@Value("${sso.redirectUri_pub}")
	private String redirectUriPub;
	@Value("${sso.redirectUri_admin}")
	private String redirectUriAdmin;

	@Value("${sso.adminClientId}")
	private String adminClientId;
	@Value("${sso.adminClientSecret}")
	private String adminClientSecret;

	@Value("${sso.tokenEndpoint}")
	private String tokenEndpoint;
	@Value("${sso.publicKeyEndPoint}")
	private String publicKeyEndPoint;

	@Value("${sso.grantTypeAuth}")
	private String grantAuthorizationCode;
	@Value("${sso.grantTypeClient}")
	private String grantClientCredentials;
	@Value("${sso.grantTypePassword}")
	private String grantPasswordCredentials;
	@Value("${sso.grantTypeRefresh}")
	private String grantRefreshToken;
	@Value("${sso.userMethod}")
	private String userMethod;
	@Value("${sso.logoutMethod}")
	private String logoutMethod;
	
	@Value("${sso.cookieInTokenName}")
	private String COOKIE_IN_TOKEN_NAME;

	@Value("${cityHub.url}")
	private String cityHubUrl;

	private OkHttpClient client = new OkHttpClient();

	private static String user_token;
	private static String user_refreshToken = "";

	/**
	 * String token 쿠키에서 토큰을 파싱해 반환
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String getAccessTokenFromCookie(HttpServletRequest request, String requestUrl) throws Exception {
		HttpSession session = request.getSession();

		Cookie[] cookies = request.getCookies();
		String accessToken = null;
		if (cookies != null) {
			for (Cookie itr : cookies) {
				logger.info(itr.toString());
				if (itr.getName().equals(COOKIE_IN_TOKEN_NAME)) {
					accessToken = itr.getValue();
					break;
				}
			}
		}
		if (accessToken == null && MakeUtil.isNotNullAndEmpty(session.getAttribute("accessToken"))) {
			accessToken = "" + session.getAttribute("accessToken");
		}

		return accessToken;
	}

	/**
	 * auth코드 요청을위한 uri 반환
	 * 
	 * @param request
	 * @return
	 */
	public String getAuthCode(HttpServletRequest request, String requestUrl) {
		String state = EncryptionUtil.sha256Encoder(request);
		String redirectUri = redirectUriPub;

		String urlParam = "?response_type=" + responseType + "&redirect_uri=" + redirectUri + "&client_id=" + adminClientId + "&state=" + state + "";

		return dataHubUrlPubAdmin + authEndpoint + urlParam;
	}

	/**
	 * token 생성(가져오기)
	 * 
	 * @param code
	 * @return
	 */
	public String getTokenByAuthorizationCode(String code, String requestUrl) {

		String tokenUrl = dataHubUrlPubAdmin + tokenEndpoint;
		// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
		if(!"".equals(dataHubUrlPriAdmin)){
			tokenUrl = dataHubUrlPriAdmin + tokenEndpoint;
		}
		JsonObject jsonObject = new JsonObject();

		String redirectUri = redirectUriPub;
		jsonObject.addProperty("grant_type", grantAuthorizationCode);
		jsonObject.addProperty("client_id", adminClientId);
		jsonObject.addProperty("client_secret", adminClientSecret);
		jsonObject.addProperty("redirect_uri", redirectUri);
		jsonObject.addProperty("code", code);

		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
		Request okRequest = new Request.Builder().url(tokenUrl).post(requestBody).build();
		Response response = null;
		String resMessage = "";

		try {
			response = client.newCall(okRequest).execute();
			resMessage = response.body().string();
		} catch (IOException e) {
			logger.error("Error IOException getTokenByAuthorizationCode ", e);
			MakeUtil.printErrorLogger(e, "Error IOException getTokenByAuthorizationCode");
			resMessage = "connect timed out";
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Error Exception getTokenByAuthorizationCode ", e);
			MakeUtil.printErrorLogger(e, "Error Exception getTokenByAuthorizationCode");
			e.printStackTrace();
		} finally {
			if (response != null)
				response.body().close();
		}
		return resMessage;
	}

	/**
	 * Cookie 생성및 설정후 accessToken반환
	 * 
	 * @param response
	 * @param tokenResponse
	 * @return
	 */
	public Cookie cookieAddTokenByJson(HttpServletRequest request, HttpServletResponse response, String tokenResponse,
			String requestUrl) {
		JsonParser parser = new JsonParser();
		Cookie cookie = null;

		if (tokenResponse != null) {
			JsonObject token = parser.parse(tokenResponse).getAsJsonObject();
			String accessToken = "access_token";
			cookie = new Cookie(COOKIE_IN_TOKEN_NAME, token.get(accessToken).getAsString());

			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setMaxAge(60 * 60 * 24);
			response.addCookie(cookie);
		}

		return cookie;
	}

	/**
	 * 토큰값 세션에 저장
	 * 
	 * @param token
	 * @param request
	 */
	public void createTokenSession(String token, HttpServletRequest request) {
		if (token != null) {
			HttpSession session = request.getSession();
			session.setAttribute("token", token);
		}
	}

	/**
	 * accessToken 세션에 저장
	 * 
	 * @param accessToken
	 * @param request
	 */
	public void createAccessTokenSession(String accessToken, HttpServletRequest request) {
		if (accessToken != null) {
			HttpSession session = request.getSession();
			session.setAttribute("accessToken", accessToken);
		}
	}

	/**
	 * 토큰 검증후 true/false
	 * 
	 * @param publicKeyResponse
	 * @param token
	 * @param request
	 * @param response
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings({ "static-access" })
	public boolean ValidateToken(String publicKeyResponse, String accessToken, HttpServletRequest request,
			HttpServletResponse response, String option, String requestUrl)
			throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

		try {
			if (MakeUtil.isNotNullAndEmpty(publicKeyResponse)) {
				JSONObject publicKeyResponseJson = new JSONObject().fromObject(publicKeyResponse);
				publicKeyResponse = "" + publicKeyResponseJson.get("publickey");
				KeyFactory kf = KeyFactory.getInstance("RSA");

				String publicKeyContent = publicKeyResponse.replaceAll("\r\n", "").replaceAll("-----BEGIN PUBLIC KEY-----", "")
						.replaceAll("-----END PUBLIC KEY-----", "").replaceAll("\"", "");

				Decoder decoder = Base64.getDecoder();
				X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decoder.decode(publicKeyContent));

				PublicKey publicKey = kf.generatePublic(keySpecX509);

				Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(accessToken);

				if (claims.getBody().getExpiration().before(new Date())) {
					return callRefreshToken(request, response, option, requestUrl);
				}

				return true;

			} else {
				return false;
			}

		} catch (ExpiredJwtException e) {
			e.printStackTrace();
			return callRefreshToken(request, response, option, requestUrl);
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "ValidateToken Error");
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	/**
	 * api콜 응답값(공개키)
	 * 
	 * @return
	 */
	public String getPublicKey(String requestUrl) {
		String publicKeyUrl = dataHubUrlPubAdmin + publicKeyEndPoint;
		// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
		if(!"".equals(dataHubUrlPriAdmin)){
			publicKeyUrl = dataHubUrlPriAdmin + publicKeyEndPoint;
		}

		Request request = new Request.Builder().url(publicKeyUrl).get().build();
		String resMessage = "";
		Response response = null;
		try {
			response = client.newCall(request).execute();
			resMessage = response.body().string();
		} catch (IOException e) {
			MakeUtil.printErrorLogger(e, "getPublicKey Error");
		} finally {
			if (response != null)
				response.body().close();
		}

		return resMessage;
	}

	/**
	 * 쿠키 삭제
	 * 
	 * @param request
	 * @param response
	 */
	public void removeCookie(HttpServletRequest request, HttpServletResponse response, String requestUrl) {
		Cookie cookie = new Cookie(COOKIE_IN_TOKEN_NAME, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * 세션 삭제
	 * 
	 * @param request
	 */
	public void removeSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.invalidate();
	}

	/**
	 * token발급 성공시 true 실패시 false
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public boolean callRefreshToken(HttpServletRequest request, HttpServletResponse response, String option,
			String requestUrl) {
		String tokenUrl = dataHubUrlPubAdmin + tokenEndpoint;
		// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
		if(!"".equals(dataHubUrlPriAdmin)){
			tokenUrl = dataHubUrlPriAdmin + tokenEndpoint;
		}
		String base64IdPw = adminClientId + ":" + adminClientSecret;
		String refreshHeader = "Basic " + Base64Utils.encodeToString(base64IdPw.getBytes());
		String refreshToken = getRefreshTokenFromSession(request);
		JsonObject jsonObject = new JsonObject();

		if (refreshToken == null)
			return false;

		jsonObject.addProperty("grant_type", grantRefreshToken);
		if ("owner".equals(option))
			jsonObject.addProperty("refresh_token", user_refreshToken);
		else
			jsonObject.addProperty("refresh_token", refreshToken);

		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
		Request okRequest = new Request.Builder().url(tokenUrl).addHeader("Authorization", refreshHeader).post(requestBody)
				.build();
		Response okResponse = null;
		String resMessage = "";

		try {
			okResponse = client.newCall(okRequest).execute();
			resMessage = okResponse.body().string();
		} catch (IOException e) {
			MakeUtil.printErrorLogger(e, "callRefreshToken IOException Error");
			e.printStackTrace();
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "callRefreshToken Error");
			e.printStackTrace();
		} finally {
			if (okResponse != null)
				okResponse.body().close();
		}

		if (okResponse.isSuccessful()) {
			if ("owner".equals(option)) {
				if (MakeUtil.isNotNullAndEmpty(resMessage)) {
					JsonObject token = new JsonParser().parse(resMessage).getAsJsonObject();
					if (token.get("access_token") != null)
						user_token = token.get("access_token").getAsString();

					if (token.get("refresh_token") != null)
						user_refreshToken = token.get("refresh_token").getAsString();
				}
			} else {
				cookieAddTokenByJson(request, response, resMessage, requestUrl); // 쿠키
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * session에서 refreshToken 분리해서 리턴
	 * 
	 * @param request
	 * @return
	 */
	public String getRefreshTokenFromSession(HttpServletRequest request) {
		String token = (String) request.getSession().getAttribute("token");
		String refreshToken = null;
		JsonParser parser = new JsonParser();

		if (token != null) {
			JsonObject getRefreshToken = parser.parse(token).getAsJsonObject();
			String target = "refresh_token";
			if (getRefreshToken.get(target) != null) {
				refreshToken = getRefreshToken.get(target).getAsString();
			}
		}

		return refreshToken;
	}

	/**
	 * session에서 expires 분리해서 리턴
	 * 
	 * @param request
	 * @return
	 */
	public String getExpiresDateFromSession(HttpServletRequest request) {
		String token = (String) request.getSession().getAttribute("token");
		String expires = null;
		JsonParser parser = new JsonParser();

		if (token != null) {
			JsonObject getRefreshToken = parser.parse(token).getAsJsonObject();
			String target = "expires_in";
			if (getRefreshToken.get(target) != null) {
				expires = getRefreshToken.get(target).getAsString();
			}
		}

		return expires;
	}

	/**
	 * 통합포탈에서 user정보 가져오기
	 * 
	 * @param userId
	 * @param token
	 * @return
	 */
	public String getUserInfo(String userId, HttpServletRequest request, HttpServletResponse response, String requestUrl)
			throws Exception {
		if (user_token == null || user_token == "") {
			// user의 토큰발급
			String cityhubToken = getCityhubToken(requestUrl);
			if (MakeUtil.isNotNullAndEmpty(cityhubToken)) {
				JsonObject token = new JsonParser().parse(cityhubToken).getAsJsonObject();
				if (token.get("access_token") != null)
					user_token = token.get("access_token").getAsString();

				if (token.get("refresh_token") != null)
					user_refreshToken = token.get("refresh_token").getAsString();
			}
		}

		// 유효기간 체크
		ValidateToken(getPublicKey(requestUrl), user_token, request, response, "owner", requestUrl);

		String userUrl = dataHubUrlPubAdmin + userMethod + "/" + userId;
		// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
		if(!"".equals(dataHubUrlPriAdmin)){
			userUrl = dataHubUrlPriAdmin + userMethod + "/" + userId;
		}
		Request okRrequest = null;
		Response okResponse = null;
		String resMessage = "";
		try {
			okRrequest = new Request.Builder().url(userUrl).get().addHeader("Authorization", "Bearer " + user_token)
					.build();
			okResponse = client.newCall(okRrequest).execute();
			resMessage = okResponse.body().string();
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "getUserInfo Error");
			e.printStackTrace();
		} finally {
			if (okResponse != null)
				okResponse.body().close();
		}

		return resMessage;
	}

	/**
	 * 클라이언트 오너(user) 토큰값 가져오기
	 * 
	 * @return
	 */
	public String getCityhubToken(String requestUrl) {
		String tokenUrl = dataHubUrlPubAdmin + tokenEndpoint;
		String hederString = adminClientId + ":" + adminClientSecret;
		String apiheader = "Basic " + Base64Utils.encodeToString(hederString.getBytes());
		JsonObject jsonObject = new JsonObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";

		try {
			jsonObject.addProperty("grant_type", grantPasswordCredentials);
			// jsonObject.addProperty("username", username);
			// jsonObject.addProperty("password", password);

			RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
					jsonObject.toString());
			okRequest = new Request.Builder().url(tokenUrl).get().addHeader("Authorization", apiheader).post(requestBody)
					.build();
			response = client.newCall(okRequest).execute();
			resMessage = response.body().string();

		} catch (IOException e) {
			MakeUtil.printErrorLogger(e, "getCityhubToken IOException Error");
			e.printStackTrace();
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "getCityhubToken Error");
			e.printStackTrace();
		}

		if (response.isSuccessful()) {
			return resMessage;
		} else {
			return null;
		}

	}

	/**
	 * 유저 정보 세션에 넣기
	 * 
	 * @param token
	 * @param request
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("static-access")
	public void createUserSession(String token, HttpServletRequest request, HttpServletResponse response,
			String requestUrl) throws InvalidKeySpecException, NoSuchAlgorithmException {
		HttpSession session = request.getSession();

		try {
			JSONObject publicKeyResponseJson = new JSONObject().fromObject(getPublicKey(requestUrl));
			String publicKeyResponse = "" + publicKeyResponseJson.get("publickey");
			KeyFactory kf = KeyFactory.getInstance("RSA");
			String publicKeyContent = publicKeyResponse.replaceAll("\r\n", "").replaceAll("-----BEGIN PUBLIC KEY-----", "")
					.replaceAll("-----END PUBLIC KEY-----", "").replaceAll("\"", "");

			Decoder decoder = Base64.getDecoder();
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decoder.decode(publicKeyContent));

			PublicKey publicKey = kf.generatePublic(keySpecX509);
			Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);

			session.setAttribute("userType", claims.getBody().get("type"));
			session.setAttribute("userId", claims.getBody().get("userId"));
			session.setAttribute("userNickname", claims.getBody().get("nickname"));
			session.setAttribute("userEmail", claims.getBody().get("email"));
			session.setAttribute("userRole", claims.getBody().get("role"));

			// User(name, phone)정보 가져오기
			if ("Analytics_Admin".equals(session.getAttribute("userRole")))
				user_token = token;

			String userInfo = getUserInfo("" + session.getAttribute("userId"), request, response, requestUrl);
			if (MakeUtil.isNotNullAndEmpty(userInfo)) {
				JSONObject userInfoJson = new JSONObject().fromObject(userInfo);
				session.setAttribute("userPhone", userInfoJson.get("phone"));
				session.setAttribute("userName", userInfoJson.get("name"));
			}

			// cityHub URL 저장
			session.setAttribute("cityHubUrl", cityHubUrl);
		} catch (Exception e) {
			logger.error("createUserSession Error : " + e);
			MakeUtil.printErrorLogger(e, "createUserSession Error");
		}
	}

	/**
	 * 통합포털 로그아웃
	 * 
	 * @param userId
	 * @return
	 */
	public String logout(String userId, HttpServletRequest request, String requestUrl) {
		String logoutUrl = dataHubUrlPubAdmin + logoutMethod;
		// If "dataHubUrlPubAdmin" does not work, you can use "dataHubUrlPriAdmin".
		if(!"".equals(dataHubUrlPriAdmin)){
			logoutUrl = dataHubUrlPriAdmin + logoutMethod;
		}
		
		JsonObject jsonObject = new JsonObject();
		Request okRequest = null;
		Response response = null;
		String resMessage = "";

		try {
			jsonObject.addProperty("userId", userId);
			RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
					jsonObject.toString());
			okRequest = new Request.Builder().url(logoutUrl).get()
					.addHeader("Authorization", "Bearer " + getAccessTokenFromCookie(request, requestUrl)).post(requestBody)
					.build();

			response = client.newCall(okRequest).execute();
			resMessage = response.body().string();

		} catch (IOException e) {
			MakeUtil.printErrorLogger(e, "logout IOException Error");
			e.printStackTrace();
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "logout Error");
			e.printStackTrace();
		} finally {
			if (response != null)
				response.body().close();
		}

		return resMessage;
	}

	/**
	 * 사용자 role 체크
	 * 
	 * @param accessToken
	 * @param requestUrl
	 * @return
	 */
	@SuppressWarnings("static-access")
	public boolean userRoleCheck(HttpServletRequest request, HttpServletResponse response, String option, String accessToken, String requestUrl) {
		String userRole = null;
		try {
			// 세션체크
			HttpSession session = request.getSession();
			if(MakeUtil.isNotNullAndEmpty(session.getAttribute("userRole"))) {
				userRole = "" + session.getAttribute("userRole");

			}else {
				JSONObject publicKeyResponseJson = new JSONObject().fromObject(getPublicKey(requestUrl));
				String publicKeyResponse = "" + publicKeyResponseJson.get("publickey");

				KeyFactory kf = KeyFactory.getInstance("RSA");
				String publicKeyContent = publicKeyResponse.replaceAll("\r\n", "").replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll("-----END PUBLIC KEY-----", "").replaceAll("\"", "");

				Decoder decoder = Base64.getDecoder();
				X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decoder.decode(publicKeyContent));
				PublicKey publicKey = kf.generatePublic(keySpecX509);
				
				Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(accessToken);
				if(claims.getBody().getExpiration().before(new Date())) {
					return callRefreshToken(request, response, option, requestUrl);
				}
				userRole = "" + claims.getBody().get("role");

			}

			if("Analytics_Admin".equals(userRole)) {
				return true;
			}
			
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "userRoleCheck Error");
			removeCookie(request, response, requestUrl);
			removeSession(request);
		}

		return false;
	}

}
