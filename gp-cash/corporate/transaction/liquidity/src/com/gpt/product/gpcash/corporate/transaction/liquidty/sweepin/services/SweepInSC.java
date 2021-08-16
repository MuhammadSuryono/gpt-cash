package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepin.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface SweepInSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_LIQ_SWEEP_IN";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getLocalCurrency(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateSubAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeSweepBackTransaction(String parameter) throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
}
