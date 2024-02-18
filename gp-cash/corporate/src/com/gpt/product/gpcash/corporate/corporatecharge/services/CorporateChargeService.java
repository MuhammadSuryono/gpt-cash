package com.gpt.product.gpcash.corporate.corporatecharge.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CorporateChargeService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getCorporateCharges(String applicationCode, String transactionServiceCode, String corporateId)
			throws ApplicationException, BusinessException;

	Map<String, Object> getCorporateChargesPerRecord(String applicationCode, String transactionServiceCode,
			String corporateId, Long records) throws ApplicationException, BusinessException;
	
	Map<String, Object> getCorporateChargesEquivalent(String applicationCode, String transactionServiceCode,
			String customerId,String soureAccCurrency) throws ApplicationException, BusinessException;
}
