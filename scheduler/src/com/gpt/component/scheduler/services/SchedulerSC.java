package com.gpt.component.scheduler.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface SchedulerSC {
	String menuCode = "MNU_GPCASH_SCHEDULER";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> execute(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
