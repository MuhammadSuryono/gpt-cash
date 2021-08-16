package com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model.DirectDebitModel;

@AutoDiscoveryImpl
public interface DirectDebitService extends CorporateUserWorkflowService {
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(DirectDebitModel directDebitModel);
	
	void executeDirectDebitResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void transactionNotification(String directDebitId) throws Exception;
	
	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
		
}