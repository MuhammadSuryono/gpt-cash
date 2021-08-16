package com.gpt.product.gpcash.retail.logging;

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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.logging.activity.services.CustomerActivityLogSC;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;

@Aspect
@Order(0)
@Component
public class CustomerActivityLogAspect {
	
	@Autowired
	private IDMMenuRepository menuRepo;

	@Autowired
	private CustomerActivityLogSC customerActivityLogSC;
	
	@Autowired
	private MessageSource message;
	
	@SuppressWarnings("unchecked")
	@Around("@annotation(customerActivityLog)")
	public Object log(ProceedingJoinPoint joinPoint, EnableCustomerActivityLog customerActivityLog) throws Throwable {
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
			String menuCode = null, action = null, referenceNo = null, customerId = null;
			
			Map<String, Object> map = (Map<String, Object>)joinPoint.getArgs()[0];
			menuCode = (String)map.get(ApplicationConstants.STR_MENUCODE);
			action = (String)map.get("action");

			if(result instanceof Map){
				referenceNo = (String)((HashMap<String, Object>)result).get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			}
			
			customerId = (String)map.get(ApplicationConstants.CUST_ID);
			
			IDMMenuModel menu = menuRepo.findOne(menuCode);
			
			if(customerId != null && menu != null) {
				customerActivityLogSC.saveActivityLog(menuCode, menu.getName(), action, isError, errorCode, 
						errorDescription, errorTrace, referenceNo, customerId, DateUtils.getCurrentTimestamp(), MDCHelper.getTraceId());
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