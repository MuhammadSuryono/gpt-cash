package com.gpt.product.gpcash.corporate.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.logging.activity.services.CorporateActivityLogSC;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;

@Aspect
@Order(0)
@Component
public class CorporateActivityLogAspect {
	
	@Autowired
	private IDMMenuRepository menuRepo;

	@Autowired
	private CorporateActivityLogSC corporateActivityLogSC;
	
	@Autowired
	private MessageSource message;
	
	@SuppressWarnings("unchecked")
	@Around("@annotation(corporateActivityLog)")
	public Object log(ProceedingJoinPoint joinPoint, EnableCorporateActivityLog corporateActivityLog) throws Throwable {
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
			errorDescription =  message.getMessage(e.getErrorCode(), new String[]{}, null, Locale.ENGLISH);
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} catch (ApplicationException e) {
			isError = true;
			errorCode = "GPT-GENERALERROR";
			errorDescription =  message.getMessage("GPT-GENERALERROR", new String[]{}, null, Locale.ENGLISH);
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} catch (Exception e) {
			isError = true;
			errorCode = "GPT-GENERALERROR";
			errorDescription =  message.getMessage("GPT-GENERALERROR", new String[]{}, null, Locale.ENGLISH);
			errorTrace = getErrorTrace(e, 1024);
			throw e;
		} finally{
			String menuCode = null, action = null, loginId = null, referenceNo = null, corporateId = null;
			
			
			//jika action dari pending task admin
			if(joinPoint.getArgs()[0] instanceof CorporateAdminPendingTaskVO){
				CorporateAdminPendingTaskVO vo = (CorporateAdminPendingTaskVO)joinPoint.getArgs()[0];
				menuCode = vo.getMenuCode();
				action = vo.getAction();
				loginId = vo.getActionBy();
				referenceNo = vo.getReferenceNo();
				corporateId = vo.getCorporateId();
				
				String methodName = joinPoint.getSignature().getName();
				
				//overwrite action jika approve atau reject
				if(ApplicationConstants.WF_ACTION_APPROVE.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_APPROVE;
				else if(ApplicationConstants.WF_ACTION_REJECT.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_REJECT;
			
			
				//jika action dari pending task user
			} else if(joinPoint.getArgs()[0] instanceof CorporateUserPendingTaskVO){
				CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO)joinPoint.getArgs()[0];
				menuCode = vo.getMenuCode();
				action = vo.getAction();
				loginId = vo.getActionBy();
				referenceNo = vo.getReferenceNo();
				corporateId = vo.getCorporateId();
				
				String methodName = joinPoint.getSignature().getName();
				
				//overwrite action jika approve atau reject
				if(ApplicationConstants.WF_ACTION_APPROVE.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_APPROVE;
				else if(ApplicationConstants.WF_ACTION_REJECT.equalsIgnoreCase(methodName)) 
					action = ApplicationConstants.WF_ACTION_REJECT;
			} else {
				Map<String, Object> map = (Map<String, Object>)joinPoint.getArgs()[0];
				menuCode = (String)map.get(ApplicationConstants.STR_MENUCODE);
				action = (String)map.get("action");
				loginId = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
				
				if(result instanceof Map){
					referenceNo = (String)((HashMap<String, Object>)result).get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
				}
				
				corporateId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			}
			
			IDMMenuModel menu = menuRepo.findOne(menuCode);
			
			if(loginId != null && menu != null) {
				corporateActivityLogSC.saveActivityLog(menuCode, menu.getName(), action, loginId.toUpperCase(), isError, errorCode, 
						errorDescription, errorTrace, referenceNo, corporateId, DateUtils.getCurrentTimestamp(), MDCHelper.getTraceId());
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