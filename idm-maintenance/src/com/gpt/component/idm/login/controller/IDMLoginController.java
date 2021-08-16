package com.gpt.component.idm.login.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.web.security.IDMSecurityHandler;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@RestController
public class IDMLoginController extends BankBaseController {
	
	@Autowired
	protected IDMSecurityHandler securityHandler;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseBankUrl +"/login")
	public DeferredResult<Map<String, Object>> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession oldSession = request.getSession(false);
		if(oldSession != null) {
			param.put(ApplicationConstants.LOGIN_HANDLES_LOGOUT, Boolean.TRUE);
			param.put(ApplicationConstants.LOGIN_HISTORY_ID, oldSession.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID));
			param.put(ApplicationConstants.LOGIN_DATE, oldSession.getAttribute(ApplicationConstants.LOGIN_DATE));
			
			oldSession.invalidate();
		}

		param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHBO);
		param.put(ApplicationConstants.LOGIN_IPADDRESS, getLoginIPAddress(request));
		param.put("userAgent", getUserAgent(request));
		return invoke("IDMLoginSC", ApplicationConstants.BANK_LOGIN_METHOD, (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			String userId =  (String) map.get(ApplicationConstants.LOGIN_USERID);
			List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
			
			//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
			menuList.add("MNU_GPCASH_LOG_ACTV");
			menuList.add("MNU_GPCASH_MT_PARAMETER");
			menuList.add("MNU_GPCASH_IDM_CHG_PASSWD");
			menuList.add("MNU_GPCASH_IDM_ROLE_MENU");
			menuList.add("MNU_GPCASH_PENDING_TASK");
			
			Map<String, Object> resultMap = new HashMap<>();

			HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, userId, menuList, resultMap);

			String loginUserName = (String) map.get(ApplicationConstants.LOGIN_USERNAME);
			ArrayList<String> loginHistoryId = (ArrayList<String>)map.get(ApplicationConstants.LOGIN_HISTORY_ID);
			Timestamp loginDate = (Timestamp) map.get(ApplicationConstants.LOGIN_DATE);
			
			session.setAttribute(ApplicationConstants.LOGIN_USERID, userId);
			session.setAttribute(ApplicationConstants.LOGIN_USERNAME, loginUserName);
			session.setAttribute(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryId);
			session.setAttribute(ApplicationConstants.LOGIN_DATE, loginDate);
			
			resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
			resultMap.put("lastLoginDate", map.get("lastLoginDate"));
			resultMap.put("message", "success");

			deferredResult.setResult(resultMap);
		}, this::defaultOnException, param);
	}

	@RequestMapping(baseBankUrl +"/logout")
	public Map<String, Object> logout(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.invalidate();

		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("message", "success");
		
		return resultMap;
	}
	
	@RequestMapping(baseBankUrl + "/getProfiles")
	public DeferredResult<Map<String, Object>> getProfiles(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		
		//get user from session
		param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
				
		return invoke("IDMLoginSC", "getProfiles", param);
	}
	
	@RequestMapping(baseBankUrl + "/forceChangePassword")
	public DeferredResult<Map<String, Object>> forceChangePassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("IDMLoginSC", "forceChangePassword", param);
	}
	
	@RequestMapping(baseBankUrl + "/changeLanguage")
	public Map<String, Object> changeLanguage(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100159");
		return resultMap;
	}
	
	private String getLoginIPAddress(HttpServletRequest request){
		String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR"); 
		if(!ValueUtils.hasValue(ipAddress)){
			ipAddress = request.getHeader("Remote_Addr");

			if(!ValueUtils.hasValue(ipAddress)){
			    ipAddress = request.getRemoteAddr();
			}
		}
		
		return ipAddress;
	}
	
	private String getUserAgent(HttpServletRequest request){
		String userAgent = request.getHeader("User-Agent"); 
		if(!ValueUtils.hasValue(userAgent)){
			userAgent = ApplicationConstants.EMPTY_STRING;
		}
		
		return userAgent;
	}
	
	@RequestMapping(baseBankUrl + "/heartBeat")
	public Map<String, Object> heartBeat(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("heartBeat", Helper.getRandomPassword(16));
		return resultMap;
	}
}
