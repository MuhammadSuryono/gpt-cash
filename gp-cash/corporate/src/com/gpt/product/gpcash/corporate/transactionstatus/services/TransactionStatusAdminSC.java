package com.gpt.product.gpcash.corporate.transactionstatus.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface TransactionStatusAdminSC {
	String menuCode = "MNU_GPCASH_F_FIN_ACTV";

	Map<String, Object> searchUserGroupMenu(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
