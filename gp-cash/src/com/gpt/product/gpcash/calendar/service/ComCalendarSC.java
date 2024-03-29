package com.gpt.product.gpcash.calendar.service;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ComCalendarSC extends WorkflowService {
	String menuCode = "MNU_GPCASH_MT_CALENDAR";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitUpdate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
}
