package com.gpt.product.gpcash.retail.transactionstatus.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerTransactionStatusSC {
	String menuCode = "MNU_R_GPCASH_F_TRX_STATUS";

	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchUserMenu(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
