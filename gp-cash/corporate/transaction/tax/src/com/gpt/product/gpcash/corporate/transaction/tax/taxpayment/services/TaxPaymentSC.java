package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface TaxPaymentSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_TAX_PAYMENT";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> npwpInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTaxType(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchDepositType(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchIdentityType(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchKPPForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}
