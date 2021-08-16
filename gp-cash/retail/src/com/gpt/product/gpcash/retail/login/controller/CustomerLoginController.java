package com.gpt.product.gpcash.retail.login.controller;

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
public class CustomerLoginController extends CustomerUserBaseController {

	@Autowired
	protected IDMSecurityHandler securityHandler;

	@Autowired
	protected HeartBeatHandler heartbeatHandler;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCustUserUrl + "/login")
	public DeferredResult<Map<String, Object>> customerLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession oldSession = request.getSession(false);
		if(oldSession != null) {
			param.put(ApplicationConstants.LOGIN_HANDLES_LOGOUT, Boolean.TRUE);
			param.put(ApplicationConstants.LOGIN_HISTORY_ID, oldSession.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID));
			param.put(ApplicationConstants.LOGIN_DATE, oldSession.getAttribute(ApplicationConstants.LOGIN_DATE));
			
			oldSession.invalidate();
		}
		
		param.put(ApplicationConstants.LOGIN_IPADDRESS, getLoginIPAddress(request));
		param.put("userAgent", getUserAgent(request));
		param.put(ApplicationConstants.LOGIN_USERID, (String) param.get(ApplicationConstants.LOGIN_USERID));

		return invoke("CustomerLoginSC", ApplicationConstants.CUST_LOGIN_METHOD, (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			String custId =  (String) map.get(ApplicationConstants.CUST_ID);

			Map<String, Object> resultMap = new HashMap<>();
			
			//yg di letakin menu di session adalah menu2 AP
			//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
			//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
			//START remark if stress test
			List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
			
			//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
			menuList.add("MNU_R_GPCASH_F_NON_FIN");
			menuList.add("MNU_GPCASH_MT_PARAMETER");
			menuList.add("MNU_R_GPCASH_F_CHG_PASSWD");
			menuList.add("MNU_R_GPCASH_F_USER_DASHBOARD");
			menuList.add("MNU_GPCASH_MT_HOST_ERROR_MAPPING");

			HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, custId, menuList, resultMap);
			
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
			
			session.setAttribute(ApplicationConstants.CUST_ID, (String) map.get(ApplicationConstants.CUST_ID));
			session.setAttribute(ApplicationConstants.LOGIN_USERID, (String) map.get(ApplicationConstants.CUST_ID)); //for security
			session.setAttribute(ApplicationConstants.LOGIN_USERNAME, loginUserName);
			session.setAttribute(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryId);
			session.setAttribute(ApplicationConstants.LOGIN_DATE, loginDate);
			

			resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
			resultMap.put("lastLoginDate", map.get("lastLoginDate"));
			//END remark if stress test
			resultMap.put("message", "success");
			
			deferredResult.setResult(resultMap);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCustUserUrl +"/logout")
	public Map<String, Object> logout(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.invalidate();

		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("message", "success");
		
		return resultMap;
	}
	
	@RequestMapping(baseCustUserUrl +"/getProfiles")
	public DeferredResult<Map<String, Object>> getProfiles(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		
		//get user from session
		param.put(ApplicationConstants.CUST_ID, session.getAttribute(ApplicationConstants.CUST_ID));
		
		return invoke("CustomerSC", "getUserProfiles", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			if(map.get(ApplicationConstants.LOGIN_TOKEN_NO) != null) {
				session.setAttribute(ApplicationConstants.LOGIN_TOKEN_NO, (String) map.get(ApplicationConstants.LOGIN_TOKEN_NO));
			}
			
			
			deferredResult.setResult(map);
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
	
	@RequestMapping(baseCustUserUrl +"/getHeaderName")
	public DeferredResult<Map<String, Object>> getHeaderName(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("SysParamSC", "getProductName", param);
	}
	
	@RequestMapping(baseCustUserUrl +"/forceChangePassword")
	public DeferredResult<Map<String, Object>> forceChangePassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("CustomerSC", "forceChangePassword", param);
	}
	
	@RequestMapping(baseCustUserUrl + "/changeLanguage")
	public Map<String, Object> changeLanguage(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100159");
		return resultMap;
	}
	
	@RequestMapping(baseCustUserUrl + "/heartBeat")
	public Map<String, Object> heartBeat(@RequestBody Map<String, Object> param) {
		String custId = (String)param.get(ApplicationConstants.LOGIN_USERID);
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("heartBeat", heartbeatHandler.getHeartBeat(custId));
		return resultMap;
	}
	
}
