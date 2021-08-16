package com.gpt.product.gpcash.corporate.transaction.bulkpayment.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.model.BulkPaymentModel;

@AutoDiscoveryImpl
public interface BulkPaymentService extends CorporateUserWorkflowService {
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(BulkPaymentModel bulkPaymentModel);
	
	void executeBulkPaymentResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBulkPaymentVAResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBulkPaymentUpdateHeaderScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void transactionNotification(String bulkPaymentId) throws Exception;
	
	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
		
}
