package com.gpt.component.maintenance.interestrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface InterestRateService extends WorkflowService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void validateDetail(String productCode, String balance, String period) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchInterestDetailById(Map<String, Object> map) throws ApplicationException, BusinessException;
}
