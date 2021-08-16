package com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface GrantDebitSC extends WorkflowService {
	String menuCode = "MNU_GPCASH_MT_GRANT_DEBIT";
	
	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException;
}
