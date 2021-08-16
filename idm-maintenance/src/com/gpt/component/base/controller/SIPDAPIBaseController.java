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

public class SIPDAPIBaseController extends BaseController {
	
	public static final String baseSIPDUrl = "/sipd";
	
	private static final Set<String> uncheckedMethods = 
		Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
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


			}
			
			if(logger.isDebugEnabled())
				logger.debug("[preInvoke] - [{}] - {}#{}({})", session!=null ? session.getId() : "-", serviceName, methodName, param);
			
		}
	}

}
