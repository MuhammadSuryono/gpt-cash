package com.gpt.product.gpcash.corporate.login.controller;

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

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.web.security.HeartBeatHandler;
import com.gpt.component.idm.web.security.IDMSecurityHandler;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@RestController
public class CorporateLoginController extends CorporateAdminBaseController {

	@Autowired
	protected IDMSecurityHandler securityHandler;

	@Autowired
	protected HeartBeatHandler heartbeatHandler;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpAdminUrl + "/login")
	public DeferredResult<Map<String, Object>> corporateLogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		HttpSession oldSession = request.getSession(false);
		if(oldSession != null) {
			param.put(ApplicationConstants.LOGIN_HANDLES_LOGOUT, Boolean.TRUE);
			param.put(ApplicationConstants.LOGIN_HISTORY_ID, oldSession.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID));
			param.put(ApplicationConstants.LOGIN_DATE, oldSession.getAttribute(ApplicationConstants.LOGIN_DATE));
			
			oldSession.invalidate();
		}
		
		param.put(ApplicationConstants.LOGIN_IPADDRESS, getLoginIPAddress(request));
		param.put("userAgent", getUserAgent(request));
		param.put(ApplicationConstants.LOGIN_USERCODE, Helper.getCorporateUserCode((String) param.get(ApplicationConstants.LOGIN_CORP_ID)
				, (String) param.get(ApplicationConstants.LOGIN_USERID)));

		return invoke("CorporateLoginSC", ApplicationConstants.CORP_LOGIN_METHOD, (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			String userCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);

			Map<String, Object> resultMap = new HashMap<>();
			
			//yg di letakin menu di session adalah menu2 AP
			//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
			//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
			//START remark if stress test
			List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
			
			//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
			menuList.add("MNU_GPCASH_F_NON_FIN");
			menuList.add("MNU_GPCASH_MT_PARAMETER");
			menuList.add("MNU_GPCASH_F_CHG_PASSWD");
			menuList.add("MNU_GPCASH_F_ADMIN_DASHBOARD");
			menuList.add("MNU_GPCASH_F_USER_DASHBOARD");
			menuList.add("MNU_GPCASH_MT_HOST_ERROR_MAPPING");
			menuList.add("MNU_GPCASH_F_ADMIN_PENDING_TASK");//untuk view approval
			menuList.add("MNU_GPCASH_F_PENDING_TASK"); //untuk view approval

			HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, userCode, menuList, resultMap);
			
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
			
			//for one signer SME
			session.setAttribute(ApplicationConstants.IS_ONE_SIGNER, (String) map.get(ApplicationConstants.IS_ONE_SIGNER));


			resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
			resultMap.put("lastLoginDate", map.get("lastLoginDate"));
			//END remark if stress test
			resultMap.put("message", "success");
			
			deferredResult.setResult(resultMap);
		}, this::defaultOnException, param);
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(baseCorpAdminUrl + "/loginOSAdmin")
	public DeferredResult<Map<String, Object>> corporateLoginOSAdmin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		/*HttpSession oldSession = request.getSession(false);
		if(oldSession != null) {
			param.put(ApplicationConstants.LOGIN_HANDLES_LOGOUT, Boolean.TRUE);
			param.put(ApplicationConstants.LOGIN_HISTORY_ID, oldSession.getAttribute(ApplicationConstants.LOGIN_HISTORY_ID));
			param.put(ApplicationConstants.LOGIN_DATE, oldSession.getAttribute(ApplicationConstants.LOGIN_DATE));
			
			oldSession.invalidate();
		}*/
		
		param.put(ApplicationConstants.LOGIN_IPADDRESS, getLoginIPAddress(request));
		param.put("userAgent", getUserAgent(request));
		
	
			return invoke("CorporateLoginSC", ApplicationConstants.CORP_LOGIN_OS_METHOD, (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
				String userCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);
	
				Map<String, Object> resultMap = new HashMap<>();
				
				//yg di letakin menu di session adalah menu2 AP
				//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
				//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
				//START remark if stress test
				List<String> menuList = (List<String>)map.get(ApplicationConstants.LOGIN_MENULIST);
				
				//add menu yg tidak perlu ada menu tree tp ingin bisa di akses
				menuList.add("MNU_GPCASH_F_NON_FIN");
				menuList.add("MNU_GPCASH_MT_PARAMETER");
				menuList.add("MNU_GPCASH_F_CHG_PASSWD");
				menuList.add("MNU_GPCASH_F_ADMIN_DASHBOARD");
				menuList.add("MNU_GPCASH_F_USER_DASHBOARD");
				menuList.add("MNU_GPCASH_MT_HOST_ERROR_MAPPING");
				menuList.add("MNU_GPCASH_F_ADMIN_PENDING_TASK");//untuk view approval
				menuList.add("MNU_GPCASH_F_PENDING_TASK"); //untuk view approval
	
				HttpSession session = securityHandler.handleSessionAndAuthorizations(request, response, userCode, menuList, resultMap);
				
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
				
				//for one signer SME
				session.setAttribute(ApplicationConstants.IS_ONE_SIGNER, (String) map.get(ApplicationConstants.IS_ONE_SIGNER));
	
				resultMap.put(ApplicationConstants.LOGIN_USERNAME, loginUserName);
				resultMap.put("lastLoginDate", DateUtils.getCurrentTimestamp());
				
				resultMap.put(ApplicationConstants.LOGIN_USERID, (String) map.get(ApplicationConstants.LOGIN_USERID));
				resultMap.put(ApplicationConstants.LOGIN_CORP_ID, (String) map.get(ApplicationConstants.LOGIN_CORP_ID));
				resultMap.put(ApplicationConstants.LOGIN_USERCODE, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
				
				//END remark if stress test
				resultMap.put("message", "success");
				
				deferredResult.setResult(resultMap);
			}, this::defaultOnException, param);
		
	}
	
	@RequestMapping(baseCorpAdminUrl +"/logout")
	public Map<String, Object> logout(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.invalidate();

		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("message", "success");
		
		return resultMap;
	}
	
	@RequestMapping(baseCorpAdminUrl +"/getProfiles")
	public DeferredResult<Map<String, Object>> getProfiles(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		
		//get user from session
		param.put(ApplicationConstants.LOGIN_CORP_ID, session.getAttribute(ApplicationConstants.LOGIN_CORP_ID));
		param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
		param.put(ApplicationConstants.LOGIN_USERCODE, session.getAttribute(ApplicationConstants.LOGIN_USERCODE));
		
		return invoke("CorporateUserSC", "getUserProfiles", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
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
	
	@RequestMapping(baseCorpAdminUrl +"/getHeaderName")
	public DeferredResult<Map<String, Object>> getHeaderName(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("SysParamSC", "getProductName", param);
	}
	
	@RequestMapping(baseCorpAdminUrl +"/forgotPassword")
	public DeferredResult<Map<String, Object>> forgotPassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("CorporateUserSC", "forgotPassword", param);
	}
	
	@RequestMapping(baseCorpAdminUrl +"/forceChangePassword")
	public DeferredResult<Map<String, Object>> forceChangePassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		return invoke("CorporateUserSC", "forceChangePassword", param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/changeLanguage")
	public Map<String, Object> changeLanguage(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100159");
		return resultMap;
	}
	
	@RequestMapping(baseCorpAdminUrl + "/heartBeat")
	public Map<String, Object> heartBeat(@RequestBody Map<String, Object> param) {
		String corpId = (String)param.get(ApplicationConstants.LOGIN_CORP_ID);
		String userId = (String)param.get(ApplicationConstants.LOGIN_USERID);

		String userCode = (corpId != null && userId != null) ? Helper.getCorporateUserCode(corpId, userId) : null;
		
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put("heartBeat", heartbeatHandler.getHeartBeat(userCode));
		return resultMap;
	}
	
}
