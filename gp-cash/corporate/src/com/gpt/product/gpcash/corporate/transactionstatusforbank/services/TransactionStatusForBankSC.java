package com.gpt.product.gpcash.corporate.transactionstatusforbank.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface TransactionStatusForBankSC {
	String menuCode = "MNU_GPCASH_BO_RPT_TRX_STS";

	Map<String, Object> searchCorporateMenu(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchUserMaker(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTransactionStatusByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
}