package com.gpt.component.maintenance.denomprepaid.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface PrepaidDenominationSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_MT_PREPAID_DENOM";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchDenomTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}