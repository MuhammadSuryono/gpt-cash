package com.gpt.component.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.logging.services.ActivityLogSC;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Aspect
@Order(0)
@Component
public class ActivityLogAspect {

	@Autowired
	private IDMMenuRepository menuRepo;
	
	@Autowired
	private ActivityLogSC activityLogSC;
	
	@SuppressWarnings("unchecked")
	@Around("@annotation(activityLog)")
	public Object log(ProceedingJoinPoint joinPoint, EnableActivityLog activityLog) throws Throwable {
		boolean isError = false;
		String errorCode = ApplicationConstants.EMPTY_STRING;
		String errorDescription = ApplicationConstants.EMPTY_STRING;
		String errorTrace = ApplicationConstants.EMPTY_STRING;
		
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (BusinessException e) {
			isError = true;
			errorCode = e.getErrorCode();
			errorDescription =  e.getErrorCode(); // dr error code diambil error description. Error description diambil di class BusinessException atau di tiap SC ?
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} catch (ApplicationException e) {
			isError = true;
			errorCode = "GPT-0100000";
			errorDescription =  "GPT-0100000"; // dr error code diambil error description. Error description diambil di class BusinessException atau di tiap SC ?
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} catch (Exception e) {
			isError = true;
			errorCode = "GPT-0100000";
			errorDescription =  "GPT-0100000"; // dr error code diambil error description. Error description diambil di class BusinessException atau di tiap SC ?
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} finally{
			String menuCode = null, action = null, loginId = null, referenceNo = null;
			
			if(joinPoint.getArgs()[0] instanceof PendingTaskVO){
				PendingTaskVO vo = (PendingTaskVO)joinPoint.getArgs()[0];
				menuCode = vo.getMenuCode();
				action = vo.getAction();
				loginId = vo.getActionBy();
				referenceNo = vo.getReferenceNo();
				
				String methodName = joinPoint.getSignature().getName();
				
				//overwrite action jika approve atau reject
				if(ApplicationConstants.WF_ACTION_APPROVE.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_APPROVE;
				else if(ApplicationConstants.WF_ACTION_REJECT.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_REJECT;
			}else{
				Map<String, Object> map = (Map<String, Object>)joinPoint.getArgs()[0];
				menuCode = (String)map.get(ApplicationConstants.STR_MENUCODE);
				action = (String)map.get("action");
				loginId = (String)map.get("loginId");
				
				if(result instanceof Map){
					referenceNo = (String)((HashMap<String, Object>)result).get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
				}
				
			}
			
			IDMMenuModel menu = menuRepo.findOne(menuCode);
			
			if(loginId != null && menu != null) {
				activityLogSC.saveActivityLog(menuCode, menu.getName(), action, loginId.toUpperCase(), isError, errorCode, 
						errorDescription, errorTrace, referenceNo, DateUtils.getCurrentTimestamp(), MDCHelper.getTraceId());
			}
			
		}
		
		return result;
	}
	
	private String getErrorTrace(Exception e, int length){
		StringWriter sw = new StringWriter(length);
		e.printStackTrace(new PrintWriter(sw));
		String msg = sw.toString();
		
		if(msg.length()>length) {
			return msg.substring(0, length);
		} else
			return msg;
	}
	
}
