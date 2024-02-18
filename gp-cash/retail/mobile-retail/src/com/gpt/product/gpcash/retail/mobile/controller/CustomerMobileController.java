package com.gpt.product.gpcash.retail.mobile.controller;

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

import com.gpt.component.base.controller.CustomerUserBaseController;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.web.security.HeartBeatHandler;
import com.gpt.component.idm.web.security.IDMSecurityHandler;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CustomerMobileController extends CustomerUserBaseController {

	@Autowired
	protected IDMSecurityHandler securityHandler;
	
	@Autowired
	protected HeartBeatHandler heartbeatHandler;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/MNU_R_GPCASH_M_OTP/validate")
	public DeferredResult<Map<String, Object>> validateOTP(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		List<String> menuList = (List<String>)session.getAttribute("savedMenuList");
		param.put("device", session.getAttribute("device"));
		if(menuList == null ) {
			// user is using the device for the first time
			return invoke("CustomerMobileSC", "finalizeSetup", (DeferredResult<Map<String, Object>> deferredResult, Object result) -> {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("message", "success");
				
				if(Boolean.TRUE.equals(param.get("fpAvailable"))) {
					List<String> newMenuList = new ArrayList<>();
					newMenuList.add("MNU_R_GPCASH_M_FP");  // give access right for Registering FP only
					
					String custId =  (String) session.getAttribute(ApplicationConstants.CUST_ID);
					securityHandler.handleSessionAndAuthorizations(request, response, custId, newMenuList, resultMap);
				} else {
					session.invalidate();
				}
				
				deferredResult.setResult(resultMap);
				
			}, this::defaultOnException, param);				
		} else {
			// user is trying to login using new device
			return invoke("CustomerMobileSC", "activateDevice", (DeferredResult<Map<String, Object>> deferredResult, Object result) -> {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("message", "success");
				
				session.removeAttribute("device");
				session.removeAttribute("savedMenuList");
				
				String custId =  (String) session.getAttribute(ApplicationConstants.CUST_ID);
				securityHandler.handleSessionAndAuthorizations(request, response, custId, menuList, resultMap);

				deferredResult.setResult(resultMap);
			}, this::defaultOnException, param);				
		}
	}
	
	@RequestMapping(baseCustUserUrl + "/MNU_R_GPCASH_M_OTP/getChallenge")
	public DeferredResult<Map<String, Object>> getChallenge(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		DeferredResult<Map<String, Object>> deferredResult = new DeferredResult<>();
		
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		deferredResult.setResult(map);
		
		return deferredResult;
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
		
		String userId = (String) param.get(ApplicationConstants.LOGIN_USERID);

		param.put("device", deviceInfo);
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userId)); // we never use the key provided by the UI
		param.put(ApplicationConstants.LOGIN_USERID, userId);	
		
		return param;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/authenticate")
	public DeferredResult<Map<String, Object>> authenticate(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		
		Map<String, Object> deviceInfo = (HashMap<String, Object>)param.get("device");
		
		param = prepareAuthenticationParam(request, param);
		String userId = (String) param.get(ApplicationConstants.LOGIN_USERID);
		
		return invoke("CustomerMobileSC", "authenticate", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			String custId =  (String) map.get(ApplicationConstants.CUST_ID);
			
			Map<String, Object> resultMap = new HashMap<>();
			
			// grant provisioning only
			List<String> menuList = new ArrayList<>();
			menuList.add("MNU_R_GPCASH_M_OTP"); // for OTP validation
			
			HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, custId, menuList, resultMap);
			Timestamp loginDate = (Timestamp) map.get(ApplicationConstants.LOGIN_DATE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("session id    : " + session.getId());
				logger.debug("custId      : " + custId);
				logger.debug("\n\n\n\n\n");
			}
			
			session.setAttribute(ApplicationConstants.CUST_ID, custId);
			session.setAttribute(ApplicationConstants.LOGIN_USERID, userId);
			session.setAttribute(ApplicationConstants.LOGIN_DATE, loginDate);
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
		
		String custId =  (String) map.get(ApplicationConstants.CUST_ID);
		List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
		
		//yg di letakin menu di session adalah menu2 AP
		//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
		//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
		//START remark if stress test
		//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
		menuList.add("MNU_R_GPCASH_F_NON_FIN");
		menuList.add("MNU_GPCASH_MT_PARAMETER");
		menuList.add("MNU_R_GPCASH_F_CHG_PASSWD");
		menuList.add("MNU_R_GPCASH_F_USER_DASHBOARD");
		menuList.add("MNU_GPCASH_MT_HOST_ERROR_MAPPING");
		menuList.add("MNU_R_GPCASH_F_FUND_BENEFICIARY");
		menuList.add("MNU_R_GPCASH_M_OTP");

		List<String> savedMenuList = null;
		Boolean otp = (Boolean)map.get("otp");
		if(otp != null) {
			// need provisioning, user must pass the provisiong process before granted full menu access
			savedMenuList =  menuList;

			// grant provisioning only
			menuList = new ArrayList<>();
			menuList.add("MNU_R_GPCASH_M_OTP");
			menuList.add("MNU_R_GPCASH_F_FUND_BENEFICIARY");
			
			resultMap.put("provisioning", true);
			resultMap.put("phoneNo", map.get("phoneNo"));
		}
		
		HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, custId, menuList, resultMap);

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
			logger.debug("custId      : " + custId);
			logger.debug("loginMenuList : " + menuList);
			logger.debug("\n\n\n\n\n");
		}
		
		session.setAttribute(ApplicationConstants.CUST_ID, custId);
		session.setAttribute(ApplicationConstants.LOGIN_USERID, (String) map.get(ApplicationConstants.CUST_ID)); //for security
		session.setAttribute(ApplicationConstants.LOGIN_USERNAME, loginUserName);
		session.setAttribute(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryId);
		session.setAttribute(ApplicationConstants.LOGIN_DATE, loginDate);

		resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
		resultMap.put("forceChangePassword", map.get("forceChangePassword"));
		resultMap.put("lastLoginDate", map.get("lastLoginDate"));
		//END remark if stress test
		resultMap.put("message", "success");
		
		deferredResult.setResult(resultMap);		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/mobileCustFPLogin")
	public DeferredResult<Map<String, Object>> mobileCustFPLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		Map<String, Object> deviceInfo = (Map<String, Object>)param.get("device");

		param = prepareAuthenticationParam(request, param);
		
		return invoke("CustomerMobileSC", "mobileCustFPLogin", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			loginSuccessHandler(request, response, deviceInfo, deferredResult, map);
		}, this::defaultOnException, param);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/mobileCustLogin")
	public DeferredResult<Map<String, Object>> mobileCustLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		Map<String, Object> deviceInfo = (Map<String, Object>)param.get("device");

		param = prepareAuthenticationParam(request, param);
		
		return invoke("CustomerMobileSC", "mobileCustLogin", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			loginSuccessHandler(request, response, deviceInfo, deferredResult, map);
		}, this::defaultOnException, param);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/MNU_R_GPCASH_M_FP/registerFP")
	public DeferredResult<Map<String, Object>> registerFP(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		String custId = (String) session.getAttribute(ApplicationConstants.CUST_ID);
		String userId = (String) session.getAttribute(ApplicationConstants.LOGIN_USERID);
		
		Map<String, Object> deviceInfo = (HashMap<String, Object>)session.getAttribute("device");
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userId)); // we never use the key provided by the UI
		param.put("device", deviceInfo);
		
		return invoke("CustomerMobileSC", "registerFP", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("message", "success");
			
			session.invalidate();
			
			deferredResult.setResult(resultMap);
	
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCustUserUrl +"/mobileForceChangePassword")
	public DeferredResult<Map<String, Object>> mobileForceChangePassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		String userId = (String) param.get(ApplicationConstants.LOGIN_USERID);
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userId)); // we never use the key provided by the UI
		return invoke("CustomerMobileSC", "mobileForceChangePassword", param);
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
