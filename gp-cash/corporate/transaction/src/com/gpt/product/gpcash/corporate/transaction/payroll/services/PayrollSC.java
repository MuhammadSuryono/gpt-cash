package com.gpt.product.gpcash.corporate.transaction.payroll.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;

@AutoDiscoveryImpl
public interface PayrollSC extends CorporateUserWorkflowService {
	String menuCode = PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_PAYROLL;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getFileFormats(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executePayrollResponseScheduler(String parameter) throws ApplicationException, BusinessException;

	void executePayrollVAResponseScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executePayrollUpdateHeaderScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailCreatedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException;	

	Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
