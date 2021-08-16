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

public class CorporateAdminBaseController extends BaseController {
	
	public static final String baseCorpAdminUrl = "/corp";
	
	private static final Set<String> uncheckedMethods = 
		Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
				ApplicationConstants.CORP_LOGIN_METHOD,
				ApplicationConstants.CORP_LOGIN_OS_METHOD,
				"forceChangePassword",
				"forgotPassword",
				"changeLanguage",
				"getTokenAPI",
				"postDataSIPD",
				"inquirySIPD",
				"checkStatusSIPD"
			)
		));

	
	@SuppressWarnings("unchecked")
	protected void preInvoke(HttpServletRequest request, String serviceName, String methodName, Object... params) {
		if(params[0] instanceof Map){
			Map<String, Object>  param = (Map<String, Object>)params[0];
			param.put(ApplicationConstants.STR_MENUCODE, request.getAttribute(ApplicationConstants.STR_MENUCODE));
			param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB_ADMIN);
			
			HttpSession session = null;
			if(!uncheckedMethods.contains(methodName)) {
				session = request.getSession(false);
				param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
				param.put(ApplicationConstants.LOGIN_USERCODE, session.getAttribute(ApplicationConstants.LOGIN_USERCODE));
				param.put(ApplicationConstants.LOGIN_CORP_ID, session.getAttribute(ApplicationConstants.LOGIN_CORP_ID));
				
				
				if(session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO) != null) {
					param.put(ApplicationConstants.LOGIN_TOKEN_NO, session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO));
				}

				//only open this for development
//				if(param.get(ApplicationConstants.LOGIN_CORP_ID) != null && param.get(ApplicationConstants.LOGIN_USERID) != null) {
//					param.put(ApplicationConstants.LOGIN_USERCODE, Helper.getCorporateUserCode(((String) param.get(ApplicationConstants.LOGIN_CORP_ID)).toUpperCase(), 
//							((String) param.get(ApplicationConstants.LOGIN_USERID)).toUpperCase()));
//				}
			}
			
			if(logger.isDebugEnabled())
				logger.debug("[preInvoke] - [{}] - {}#{}({})", session!=null ? session.getId() : "-", serviceName, methodName, param);
			
		}
	}

}
