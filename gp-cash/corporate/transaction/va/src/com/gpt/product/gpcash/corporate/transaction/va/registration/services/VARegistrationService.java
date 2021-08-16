package com.gpt.product.gpcash.corporate.transaction.va.registration.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface VARegistrationService extends WorkflowService {
	Map<String, Object> searchVAListByCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccount(String corporateId) throws ApplicationException, BusinessException;

	void validateDetail(String corporateId, String accountNo, String productCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchMainAccountByCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchProductCodeByRegisteredAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateProducts(String cifId) throws ApplicationException, BusinessException;
}
