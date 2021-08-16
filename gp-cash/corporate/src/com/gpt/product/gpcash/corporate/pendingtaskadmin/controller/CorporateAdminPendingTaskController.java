package com.gpt.product.gpcash.corporate.pendingtaskadmin.controller;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CorporateAdminPendingTaskController extends CorporateAdminBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_ADMIN_PENDING_TASK";
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, 
			@RequestBody Map<String, Object> param) {
		return invoke("CorporateAdminPendingTaskSC", method, param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/detailPendingTask")
	public DeferredResult<Map<String, Object>> detailPendingTask(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		return invoke("CorporateAdminPendingTaskSC", "detailPendingTask", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			HttpSession session = request.getSession(false);
			session.setAttribute(ApplicationConstants.CHALLENGE_NO, (String) map.get(ApplicationConstants.CHALLENGE_NO));
			session.setAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO, (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/approve")
	public DeferredResult<Map<String, Object>> approve(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, session.getAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO));
		return invoke("CorporateAdminPendingTaskSC", "approve", param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/reject")
	public DeferredResult<Map<String, Object>> reject(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, session.getAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO));
		return invoke("CorporateAdminPendingTaskSC", "reject", param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/approveList")
	public DeferredResult<Map<String, Object>> approveList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put("pendingTaskList", session.getAttribute("pendingTaskList"));
		
		return invoke("CorporateAdminPendingTaskSC", "approveList", param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/rejectList")
	public DeferredResult<Map<String, Object>> rejectList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put("pendingTaskList", session.getAttribute("pendingTaskList"));
		
		return invoke("CorporateAdminPendingTaskSC", "rejectList", param);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/confirmList")
	public DeferredResult<Map<String, Object>> confirmList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.setAttribute("pendingTaskList", (ArrayList) param.get("pendingTaskList"));
		
		return invoke("CorporateAdminPendingTaskSC", "confirmList", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			HttpSession sessionAfter = request.getSession(false);
			sessionAfter.setAttribute(ApplicationConstants.CHALLENGE_NO, (String) map.get(ApplicationConstants.CHALLENGE_NO));
			
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
}
