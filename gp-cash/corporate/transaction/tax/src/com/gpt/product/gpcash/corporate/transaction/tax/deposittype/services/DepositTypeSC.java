package com.gpt.product.gpcash.corporate.transaction.tax.deposittype.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface DepositTypeSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_TAX_DEPOSIT_TYP";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTaxTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}