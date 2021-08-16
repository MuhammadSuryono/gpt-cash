package com.gpt.product.gpcash.corporate.pendingtaskuser.controller;

import java.util.ArrayList;
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
public class CorporateUserPendingTaskController extends CorporateUserBaseController {

	protected static final String menuCode = "MNU_GPCASH_F_PENDING_TASK";
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, 
			@RequestBody Map<String, Object> param) {
		return invoke("CorporateUserPendingTaskSC", method, param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/detailPendingTask")
	public DeferredResult<Map<String, Object>> detailPendingTask(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		return invoke("CorporateUserPendingTaskSC", "detailPendingTask", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			HttpSession session = request.getSession(false);
			session.setAttribute(ApplicationConstants.CHALLENGE_NO, (String) map.get(ApplicationConstants.CHALLENGE_NO));
			session.setAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO, (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/detailPendingTaskOld")
	public DeferredResult<Map<String, Object>> detailPendingTaskOld(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		return invoke("CorporateUserPendingTaskSC", "detailPendingTaskOld", param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/approve")
	public DeferredResult<Map<String, Object>> approve(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, session.getAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO));
		return invoke("CorporateUserPendingTaskSC", "approve", param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/reject")
	public DeferredResult<Map<String, Object>> reject(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, session.getAttribute(ApplicationConstants.WF_FIELD_REFERENCE_NO));
		return invoke("CorporateUserPendingTaskSC", "reject", param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/approveList")
	public DeferredResult<Map<String, Object>> approveList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put("pendingTaskList", session.getAttribute("pendingTaskList"));
		return invoke("CorporateUserPendingTaskSC", "approveList", param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/rejectList")
	public DeferredResult<Map<String, Object>> rejectList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		param.put(ApplicationConstants.CHALLENGE_NO, session.getAttribute(ApplicationConstants.CHALLENGE_NO));
		param.put("pendingTaskList", session.getAttribute("pendingTaskList"));
		
		return invoke("CorporateUserPendingTaskSC", "rejectList", param);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/confirmList")
	public DeferredResult<Map<String, Object>> confirmList(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		session.setAttribute("pendingTaskList", (ArrayList) param.get("pendingTaskList"));
		return invoke("CorporateUserPendingTaskSC", "confirmList", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			HttpSession sessionAfter = request.getSession(false);
			sessionAfter.setAttribute(ApplicationConstants.CHALLENGE_NO, (String) map.get(ApplicationConstants.CHALLENGE_NO));
			
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
}