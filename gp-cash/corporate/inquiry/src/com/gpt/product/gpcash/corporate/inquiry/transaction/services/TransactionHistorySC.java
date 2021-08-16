package com.gpt.product.gpcash.corporate.inquiry.transaction.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface TransactionHistorySC {
	
	String menuCode = "MNU_GPCASH_F_TRX_HISTORY";
	
	Map<String, Object> periodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadPeriodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadPeriodicTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> latestTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> todayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTodayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTodayTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeSOTRequestScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeSOTResponseScheduler(String parameter) throws ApplicationException, BusinessException;
}
