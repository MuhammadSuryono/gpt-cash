package com.gpt.product.gpcash.corporate.transaction.inhouse.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface InHouseTransferSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_FUND_INHOUSE";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOwnAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBeneficiaryMobile(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> checkRate(Map<String, Object> map) throws ApplicationException, BusinessException;

}