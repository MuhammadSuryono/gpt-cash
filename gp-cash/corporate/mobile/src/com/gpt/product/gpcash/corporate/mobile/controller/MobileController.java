package com.gpt.product.gpcash.corporate.mobile.controller;

import java.io.Serializable;
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

import com.gpt.component.base.controller.CorporateUserBaseController;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.web.security.HeartBeatHandler;
import com.gpt.component.idm.web.security.IDMSecurityHandler;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@RestController
public class MobileController extends CorporateUserBaseController {

	@Autowired
	protected IDMSecurityHandler securityHandler;
	
	@Autowired
	protected HeartBeatHandler heartbeatHandler;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpUserUrl + "/MNU_GPCASH_M_OTP/validate")
	public DeferredResult<Map<String, Object>> validateOTP(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		List<String> menuList = (List<String>)session.getAttribute("savedMenuList");
		param.put("device", session.getAttribute("device"));
		if(menuList == null ) {
			// user is using the device for the first time
			return invoke("MobileSC", "finalizeSetup", (DeferredResult<Map<String, Object>> deferredResult, Object result) -> {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("message", "success");
				
				if(Boolean.TRUE.equals(param.get("fpAvailable"))) {
					List<String> newMenuList = new ArrayList<>();
					newMenuList.add("MNU_GPCASH_M_FP");  // give access right for Registering FP only
					
					String userCode =  (String) session.getAttribute(ApplicationConstants.LOGIN_USERCODE);
					securityHandler.handleSessionAndAuthorizations(request, response, userCode, newMenuList, resultMap);
				} else {
					session.invalidate();
				}
				
				deferredResult.setResult(resultMap);
				
			}, this::defaultOnException, param);				
		} else {
			// user is trying to login using new device
			return invoke("MobileSC", "activateDevice", (DeferredResult<Map<String, Object>> deferredResult, Object result) -> {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("message", "success");
				
				session.removeAttribute("device");
				session.removeAttribute("savedMenuList");
				
				String userCode =  (String) session.getAttribute(ApplicationConstants.LOGIN_USERCODE);
				securityHandler.handleSessionAndAuthorizations(request, response, userCode, menuList, resultMap);

				deferredResult.setResult(resultMap);
			}, this::defaultOnException, param);				
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> prepareAuthenticationParam(HttpServletRequest request, Map<String, Object> param) {
		
		Map<String, Object> deviceInfo = (HashMap<String, Object>)param.get("device");
		
		param = (Map<String, Object>)param.get("payload");
		
		HttpSession oldSession = request.getSession(false);
		if(oldSession != null) {
			param.put(ApplicationConstants.LOGIN_HANDLES_LOGOUT, Boolean.TRUE);
			param.put(ApplicationConstants.LOGIN_HISTORY_ID, oldSession.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID));
			param.put(ApplicationConstants.LOGIN_DATE, oldSession.getAttribute(ApplicationConstants.LOGIN_DATE));
			
			oldSession.invalidate();
		}

		param.put(ApplicationConstants.LOGIN_IPADDRESS, getLoginIPAddress(request));
		param.put("userAgent", getUserAgent(request));
		
		String userCode = Helper.getCorporateUserCode((String) param.get(ApplicationConstants.LOGIN_CORP_ID), (String) param.get(ApplicationConstants.LOGIN_USERID));

		param.put("device", deviceInfo);
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userCode)); // we never use the key provided by the UI
		param.put(ApplicationConstants.LOGIN_USERCODE, userCode);	
		
		return param;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpUserUrl + "/authenticate")
	public DeferredResult<Map<String, Object>> authenticate(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		
		Map<String, Object> deviceInfo = (HashMap<String, Object>)param.get("device");
		
		param = prepareAuthenticationParam(request, param);
		
		return invoke("MobileSC", "authenticate", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			String userCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			Map<String, Object> resultMap = new HashMap<>();
			
			// grant provisioning only
			List<String> menuList = new ArrayList<>();
			menuList.add("MNU_GPCASH_M_OTP"); // for OTP validation
			
			HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, userCode, menuList, resultMap);
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("session id    : " + session.getId());
				logger.debug("userCode      : " + userCode);
				logger.debug("\n\n\n\n\n");
			}
			
			session.setAttribute(ApplicationConstants.LOGIN_CORP_ID, (String) map.get(ApplicationConstants.LOGIN_CORP_ID));
			session.setAttribute(ApplicationConstants.LOGIN_USERID, (String) map.get(ApplicationConstants.LOGIN_USERID));
			session.setAttribute(ApplicationConstants.LOGIN_DATE, (Timestamp) map.get(ApplicationConstants.LOGIN_DATE));
			session.setAttribute(ApplicationConstants.LOGIN_USERCODE, userCode);
			session.setAttribute("device", (Serializable) deviceInfo);
			
			resultMap.put("message", "success");
			resultMap.put("phoneNo", map.get("phoneNo"));
			
			deferredResult.setResult(resultMap);
		}, this::defaultOnException, param);
	}	
	
	@SuppressWarnings("unchecked")
	private void loginSuccessHandler(HttpServletRequest request, HttpServletResponse response, Map<String, Object> deviceInfo, DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) {
		Map<String, Object> resultMap = new HashMap<>();
		
		if(map.get("notAllowed") != null) {
			HttpSession session = request.getSession(false);
			if(session != null)
				session.invalidate();
			
			resultMap.put("notAllowed", true);
			deferredResult.setResult(resultMap);
			return;
		}
		
		String userCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
		
		//yg di letakin menu di session adalah menu2 AP
		//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
		//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
		//START remark if stress test
		//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
		menuList.add("MNU_GPCASH_F_NON_FIN");
		menuList.add("MNU_GPCASH_MT_PARAMETER");
		menuList.add("MNU_GPCASH_F_CHG_PASSWD");
		menuList.add("MNU_GPCASH_F_ADMIN_DASHBOARD");
		menuList.add("MNU_GPCASH_F_USER_DASHBOARD");
		menuList.add("MNU_GPCASH_MT_HOST_ERROR_MAPPING");
		menuList.add("MNU_GPCASH_F_ADMIN_PENDING_TASK");//untuk view approval
		menuList.add("MNU_GPCASH_F_PENDING_TASK"); //untuk view approval

		List<String> savedMenuList = null;
		Boolean otp = (Boolean)map.get("otp");
		if(otp != null) {
			// need provisioning, user must pass the provisiong process before granted full menu access
			savedMenuList =  menuList;

			// grant provisioning only
			menuList = new ArrayList<>(1);
			menuList.add("MNU_GPCASH_M_OTP");
			
			resultMap.put("provisioning", true);
			resultMap.put("phoneNo", map.get("phoneNo"));
		}
		
		HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, userCode, menuList, resultMap);

		if(savedMenuList != null) {
			// saved for later
			session.setAttribute("savedMenuList", savedMenuList);
			session.setAttribute("device", (Serializable) deviceInfo);
		}
		
		String loginUserName = (String) map.get(ApplicationConstants.LOGIN_USERNAME);
		ArrayList<String> loginHistoryId = (ArrayList<String>)map.get(ApplicationConstants.LOGIN_HISTORY_ID);
		Timestamp loginDate = (Timestamp) map.get(ApplicationConstants.LOGIN_DATE);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("session id    : " + session.getId());
			logger.debug("userCode      : " + userCode);
			logger.debug("loginMenuList : " + menuList);
			logger.debug("\n\n\n\n\n");
		}
		
		session.setAttribute(ApplicationConstants.LOGIN_CORP_ID, (String) map.get(ApplicationConstants.LOGIN_CORP_ID));
		session.setAttribute(ApplicationConstants.LOGIN_USERID, (String) map.get(ApplicationConstants.LOGIN_USERID));
		session.setAttribute(ApplicationConstants.LOGIN_USERCODE, userCode);
		session.setAttribute(ApplicationConstants.LOGIN_USERNAME, loginUserName);
		session.setAttribute(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryId);
		session.setAttribute(ApplicationConstants.LOGIN_DATE, loginDate);

		resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
		resultMap.put("lastLoginDate", map.get("lastLoginDate"));
		//END remark if stress test
		resultMap.put("message", "success");
		
		deferredResult.setResult(resultMap);		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpUserUrl + "/mobileCorpFPLogin")
	public DeferredResult<Map<String, Object>> mobileCorpFPLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		Map<String, Object> deviceInfo = (Map<String, Object>)param.get("device");

		param = prepareAuthenticationParam(request, param);
		
		return invoke("MobileSC", "mobileCorpFPLogin", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			loginSuccessHandler(request, response, deviceInfo, deferredResult, map);
		}, this::defaultOnException, param);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpUserUrl + "/mobileCorpLogin")
	public DeferredResult<Map<String, Object>> mobileCorpLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		Map<String, Object> deviceInfo = (Map<String, Object>)param.get("device");

		param = prepareAuthenticationParam(request, param);
		
		return invoke("MobileSC", "mobileCorpLogin", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			loginSuccessHandler(request, response, deviceInfo, deferredResult, map);
		}, this::defaultOnException, param);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpUserUrl + "/MNU_GPCASH_M_FP/registerFP")
	public DeferredResult<Map<String, Object>> registerFP(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		String userCode = (String) session.getAttribute(ApplicationConstants.LOGIN_USERCODE);
		Map<String, Object> deviceInfo = (HashMap<String, Object>)session.getAttribute("device");
		
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userCode)); // we never use the key provided by the UI
		param.put("device", deviceInfo);
		
		return invoke("MobileSC", "registerFP", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("message", "success");
			
			session.invalidate();
			
			deferredResult.setResult(resultMap);
	
		}, this::defaultOnException, param);
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
	
}
