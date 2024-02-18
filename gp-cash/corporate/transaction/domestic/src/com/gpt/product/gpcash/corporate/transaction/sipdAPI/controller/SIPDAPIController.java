package com.gpt.product.gpcash.corporate.transaction.sipdAPI.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.SIPDAPIBaseController;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.web.security.IDMSecurityHandler;

@RestController
public class SIPDAPIController extends SIPDAPIBaseController {

	@Autowired
	protected IDMSecurityHandler securityHandler;

	@RequestMapping(baseSIPDUrl + "/gettoken")
	public DeferredResult<Map<String, Object>> getTokenAPI(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessException {		
			return invoke("SIPDAPISC", "getTokenAPI", param);
		
	}
	
	@RequestMapping(baseSIPDUrl + "/postdata")
	public DeferredResult<Map<String, Object>> postDataSIPD(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) throws IOException {				
		return invoke("SIPDAPISC", "postDataSIPD", param);
	}
	

	@RequestMapping(baseSIPDUrl + "/inquiry")
	public DeferredResult<Map<String, Object>> inquirySIPD(HttpServletRequest request,HttpServletResponse response, @RequestBody Map<String, Object> param) throws IOException {		
		return invoke("SIPDAPISC", "inquirySIPD", param);		
	}
	
	@RequestMapping(baseSIPDUrl + "/checkstatus")
	public DeferredResult<Map<String, Object>> checkStatusSIPD(HttpServletRequest request, @RequestBody Map<String, Object> param) {		
		return invoke("SIPDAPISC", "checkStatusSIPD", param);
	}
	
}
 