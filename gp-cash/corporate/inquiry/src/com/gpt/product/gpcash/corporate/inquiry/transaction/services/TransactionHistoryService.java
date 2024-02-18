package com.gpt.product.gpcash.corporate.inquiry.transaction.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.IGenerateReport;

@AutoDiscoveryImpl
public interface TransactionHistoryService extends IGenerateReport {
	
	Map<String, Object> periodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadPeriodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadPeriodicTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> latestTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeSOTRequestScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeSOTResponseScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> downloadPeriodicTransactionMultiAccountForPDF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadPending(Map<String, Object> map, String requestBy) throws ApplicationException;
	
	Map<String, Object> deletePendingDownload(Map<String, Object> map) throws ApplicationException, BusinessException;
}
