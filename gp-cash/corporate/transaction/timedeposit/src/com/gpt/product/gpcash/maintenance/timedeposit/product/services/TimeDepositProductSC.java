package com.gpt.product.gpcash.maintenance.timedeposit.product.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface TimeDepositProductSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_MT_TD_PRODUCT";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}