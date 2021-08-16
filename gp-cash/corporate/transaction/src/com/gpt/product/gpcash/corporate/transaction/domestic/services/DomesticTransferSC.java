package com.gpt.product.gpcash.corporate.transaction.domestic.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface DomesticTransferSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_FUND_DOMESTIC";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBankForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	
        void executeOnlineFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeOnlineRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> checkRate(Map<String, Object> map) throws ApplicationException, BusinessException;
}
