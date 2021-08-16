package com.gpt.product.gpcash.corporate.transaction.international.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface InternationalTransferSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_FUND_INT";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBankByCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTrxPurposeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCurrencyForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBranchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}
