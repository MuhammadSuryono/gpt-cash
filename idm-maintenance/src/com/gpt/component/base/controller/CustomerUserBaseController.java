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

public class CustomerUserBaseController extends BaseController {
	
	public static final String baseCustUserUrl = "/retail";
	
	private static final Set<String> uncheckedMethods = 
			Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(
					ApplicationConstants.CUST_LOGIN_METHOD,
					"forceChangePassword",
					"forgotPassword",
					"forgotUserId",
					"changeLanguage",
					"customerVerification",
					"customerVerification2",
					"customerRegistration",
					"customerVerificationForExistingUser",
					"validateRegistrationUserId",
					"validateRegistrationExistingUserId",
					// for mobile
					"authenticate",
					"mobileCustLogin",
					"mobileForceChangePassword",
					"mobileCustFPLogin"
				)
			));

	@SuppressWarnings("unchecked")
	protected void preInvoke(HttpServletRequest request, String serviceName, String methodName, Object... params) {
		if(params[0] instanceof Map){
			Map<String, Object>  param = (Map<String, Object>)params[0];
			param.put(ApplicationConstants.STR_MENUCODE, request.getAttribute(ApplicationConstants.STR_MENUCODE));
			param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB_R);
			
			HttpSession session = null;
			if(!uncheckedMethods.contains(methodName)) {
				session = request.getSession(false);
				param.put(ApplicationConstants.CUST_ID, session.getAttribute(ApplicationConstants.CUST_ID));
				
				if(session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO) != null) {
					param.put(ApplicationConstants.LOGIN_TOKEN_NO, session.getAttribute(ApplicationConstants.LOGIN_TOKEN_NO));
				}
			}
			
			if(logger.isDebugEnabled())
				logger.debug("[preInvoke] - [{}] - {}#{}({})", session!=null ? session.getId() : "-", serviceName, methodName, param);
			
		}
	}

}