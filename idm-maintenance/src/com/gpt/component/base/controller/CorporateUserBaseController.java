package com.gpt.component.base.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.controller.BaseController;


public class CorporateUserBaseController extends BaseController {
	
	public static final String baseCorpUserUrl = "/corp";
	
	private static final Set<String> uncheckedMethods = 
			Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(
					ApplicationConstants.CORP_LOGIN_METHOD,
					"forceChangePassword",
					"forgotPassword",
					"changeLanguage",
					// for mobile
					"authenticate",
					"mobileCorpLogin",
					"mobileCorpFPLogin"
				)
			));

	@SuppressWarnings("unchecked")
	protected void preInvoke(HttpServletRequest request, String serviceName, String methodName, Object... params) {
		if(params[0] instanceof Map){
			Map<String, Object>  param = (Map<String, Object>)params[0];
			param.put(ApplicationConstants.STR_MENUCODE, request.getAttribute(ApplicationConstants.STR_MENUCODE));
			param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
			
			HttpSession session = null;
			if(!uncheckedMethods.contains(methodName)) {
				session = request.getSession(false);
				param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
				param.put(ApplicationConstants.LOGIN_USERCODE, session.getAttribute(ApplicationConstants.LOGIN_USERCODE));
				param.put(ApplicationConstants.LOGIN_CORP_ID, session.getAttribute(ApplicationConstants.LOGIN_CORP_ID));
				
				if(session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO) != null) {
					param.put(ApplicationConstants.LOGIN_TOKEN_NO, session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO));
				}
				
				if(session.getAttribute(ApplicationConstants.IS_ONE_SIGNER) != null) {
					param.put(ApplicationConstants.IS_ONE_SIGNER, session.getAttribute(ApplicationConstants.IS_ONE_SIGNER));
				}
				
				//only open this for development
//				param.put(ApplicationConstants.LOGIN_USERID, "dapprls");
//				param.put(ApplicationConstants.LOGIN_CORP_ID, "gg1");
//				param.put(ApplicationConstants.LOGIN_TOKEN_NO, "1531900319");
//				String corporateId = (String) param.get(ApplicationConstants.LOGIN_CORP_ID);
//				String userId = (String) param.get(ApplicationConstants.LOGIN_USERID);
//				if(corporateId != null && userId != null){
//					param.put(ApplicationConstants.LOGIN_USERCODE, Helper.getCorporateUserCode(corporateId.toUpperCase(), userId.toUpperCase()));
//				}		
				//end of only open this for development
			}
			
			if(logger.isDebugEnabled())
				logger.debug("[preInvoke] - [{}] - {}#{}({})", session!=null ? session.getId() : "-", serviceName, methodName, param);
			
		}
	}

}