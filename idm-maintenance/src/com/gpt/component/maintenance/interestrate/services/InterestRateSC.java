package com.gpt.component.maintenance.interestrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface InterestRateSC extends WorkflowService {

	String menuCode = "MNU_GPCASH_MT_INTEREST_RATE";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchInterestDetailById(Map<String, Object> map) throws ApplicationException, BusinessException;
}
