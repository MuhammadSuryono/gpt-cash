package com.gpt.product.gpcash.bankforexlimitusage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface BankForexLimitUsageService extends WorkflowService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void resetLimit(String parameter) throws ApplicationException, BusinessException;
	
}
