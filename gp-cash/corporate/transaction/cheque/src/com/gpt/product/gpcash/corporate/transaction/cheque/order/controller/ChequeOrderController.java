package com.gpt.product.gpcash.corporate.transaction.cheque.order.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class ChequeOrderController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_CHQ_ORDER";

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("ChequeOrderSC", method, param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/confirm")
	public DeferredResult<Map<String, Object>> confirm(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.setAttribute(ApplicationConstants.CONFIRMATION_DATA, param);
		
		return invoke("ChequeOrderSC", "confirm", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			Map<String, Object> confirmDataMap = (Map<String, Object>)session.getAttribute(ApplicationConstants.CONFIRMATION_DATA);
			confirmDataMap.putAll(map);			
			//remove unused data
			confirmDataMap.remove(ApplicationConstants.WF_ACTION);			
			//put confirmationData for submit
			session.setAttribute(ApplicationConstants.CONFIRMATION_DATA, confirmDataMap);			
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/submit")
	public DeferredResult<Map<String, Object>> approve(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);		
		Map<String, Object> confirmationDataMap = (Map<String, Object>) session.getAttribute(ApplicationConstants.CONFIRMATION_DATA);
		param.putAll(confirmationDataMap);		
		return invoke("ChequeOrderSC", "submit", param);
	}
}