package com.gpt.component.base.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.controller.BaseController;

public class BankBaseController extends BaseController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String baseBankUrl = "/bank";
	
	@SuppressWarnings("unchecked")
	protected void preInvoke(HttpServletRequest request, String serviceName, String methodName, Object... params) {
		if(params[0] instanceof Map){
			Map<String, Object>  param = (Map<String, Object>)params[0];
			param.put(ApplicationConstants.STR_MENUCODE, request.getAttribute(ApplicationConstants.STR_MENUCODE));
			param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHBO);
			
			HttpSession session = null;
			if(!methodName.equals(ApplicationConstants.BANK_LOGIN_METHOD) && !methodName.equals("forceChangePassword")) {
				session = request.getSession(false);
				param.put(ApplicationConstants.LOGIN_USERID, session.getAttribute(ApplicationConstants.LOGIN_USERID));
			}
			
			if(logger.isDebugEnabled())
				logger.debug("[preInvoke] - [{}] - {}#{}({})", session!=null ? session.getId() : "-", serviceName, methodName, param);
			
		}
	}
}
