package com.gpt.product.gpcash.corporate.changepassword.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.component.idm.web.security.HeartBeatHandler;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CorporateChangePasswordController extends CorporateAdminBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_CHG_PASSWD";

	@Autowired
	protected HeartBeatHandler heartbeatHandler;
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/changePassword")
	public DeferredResult<Map<String, Object>> changePassword(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		String userCode = (String)request.getSession(false).getAttribute(ApplicationConstants.LOGIN_USERCODE);
		
		param.put("key", heartbeatHandler.getRecordedHeartBeat(userCode)); // we never use the key provided by the UI
		
		return invoke("CorporateChangePasswordSC", "changePassword", param);
	}

	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CorporateChangePasswordSC", method, param);
	}
	
}