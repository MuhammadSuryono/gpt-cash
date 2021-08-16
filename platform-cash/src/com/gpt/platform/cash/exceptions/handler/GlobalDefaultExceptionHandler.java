package com.gpt.platform.cash.exceptions.handler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gpt.component.common.Constants;
import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.BusinessException;

@RestControllerAdvice
public class GlobalDefaultExceptionHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MessageSource message;

	@SuppressWarnings("unchecked")
	private void setMDC(HttpServletRequest req) {
		Map<String, String> mdcData = (Map<String, String>)req.getAttribute(Constants.KEY_MDC_DATA);
		if(mdcData != null)
			MDCHelper.setMDCData(mdcData);
//		else
//			MDCHelper.clearMDCData();
	}
	
	@ExceptionHandler(BusinessException.class)
	public Map<String, Object> businessExceptionHandler(Locale locale, HttpServletRequest req, HttpServletResponse resp, BusinessException e) throws Exception {
		setMDC(req);
		
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		String errorMessage = message.getMessage(e.getErrorCode(), e.getErrorArgs(), e.getErrorCode(), locale);
		
		logger.error(errorMessage, e);
		
		Map<String, Object> mav = new HashMap<>(1,1);		
		mav.put("message", errorMessage);
		
		return mav;
	}

	@ExceptionHandler(Exception.class)
	public Map<String, Object> defaultExceptionHandler(Locale locale, HttpServletRequest req, HttpServletResponse resp, Exception e) throws Exception {
		setMDC(req);

		// If the exception is annotated with @ResponseStatus rethrow it and let
		// AnnotationUtils is a Spring Framework utility class.
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
			throw e;
		
		String errorMessage = e.getMessage();
		if(errorMessage == null)
			errorMessage = e.toString();
		
		logger.error(errorMessage, e);
		
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		
		// Otherwise setup and send the user the error message
		Map<String, Object> mav = new HashMap<>(1,1);
		
		//TODO: SCU ???
//		mav.put("message", errorMessage);
		mav.put("message",  message.getMessage("GPT-GENERALERROR", null, "GPT-GENERALERROR", locale) + " [Reference No: " + MDCHelper.getTraceId() + "]");
		
		return mav;
	}
	
	private void notifyErrror(String traceId, String errorMessage) {
		try {
			//TODO error notification
			Map<String, Object> inputs = new HashMap<>();
//			inputs.put("emails", emails);
			inputs.put("subject", "Error Notification");
			
//			eaiAdapter.invokeService(EAIConstants.USER_TRANSFER_NOTIFICATION, inputs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}	
}
