package com.gpt.product.gpcash.retail.customercharge.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CustomerChargeService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getCustomerCharges(String applicationCode, String transactionServiceCode, String customerId)
			throws ApplicationException, BusinessException;

	Map<String, Object> getCustomerChargesPerRecord(String applicationCode, String transactionServiceCode,
			String customerId, Long records) throws ApplicationException, BusinessException;
	
	
	Map<String, Object> getCustomerChargesEquivalent(String applicationCode, String transactionServiceCode, String customerId, String transactionCurrency)
			throws ApplicationException, BusinessException;

	Map<String, Object> getCustomerChargesPerRecordEquivalent(String applicationCode, String transactionServiceCode,
			String customerId, Long records, String transactionCurrency) throws ApplicationException, BusinessException;
}
