package com.gpt.product.gpcash.corporate.transaction.va.registration.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface VARegistrationSC extends WorkflowService {
	String menuCode = "MNU_GPCASH_CORP_VA";
	
	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchVAByCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateProducts(Map<String, Object> map) throws ApplicationException, BusinessException;

}
