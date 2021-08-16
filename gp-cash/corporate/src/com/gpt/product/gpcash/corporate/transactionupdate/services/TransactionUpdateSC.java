package com.gpt.product.gpcash.corporate.transactionupdate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface TransactionUpdateSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_BO_TRX_UPDATE";

	Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTransactionStatusByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
}