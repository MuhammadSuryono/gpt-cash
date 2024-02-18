package com.gpt.product.gpcash.retail.transactionstatus.services;

import java.sql.Timestamp;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionActivityType;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

@AutoDiscoveryImpl
public interface CustomerTransactionStatusService {

	Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	void addTransactionStatus(String pendingTaskId, Timestamp activityDate, CustomerTransactionActivityType actionType, String user, CustomerTransactionStatus status, String eaiRefN, boolean isError, String errorCode);

	Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTransactionStatusForBank(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
