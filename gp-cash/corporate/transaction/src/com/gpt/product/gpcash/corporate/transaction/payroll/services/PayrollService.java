package com.gpt.product.gpcash.corporate.transaction.payroll.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.payroll.model.PayrollModel;

@AutoDiscoveryImpl
public interface PayrollService extends CorporateUserWorkflowService {
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(PayrollModel payrollModel);
	
	void executePayrollResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executePayrollVAResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executePayrollUpdateHeaderScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void transactionNotification(String payrollId) throws Exception;
	
	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
		
}
