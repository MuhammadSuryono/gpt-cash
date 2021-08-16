package com.gpt.product.gpcash.corporate.inquiry.vatransaction.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface VATransactionHistoryService {
	
	Map<String, Object> periodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadPeriodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadPeriodicTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> latestTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeVASOTRequestScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeVASOTResponseScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> downloadPeriodicTransactionMultiAccountForPDF(Map<String, Object> map)
			throws ApplicationException, BusinessException;
}
