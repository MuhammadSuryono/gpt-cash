package com.gpt.product.gpcash.corporate.transaction.purchase.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface PurchaseSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_PURCHASE";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPayee(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPurchaseInstitutionCategoryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPurchaseInstitutionByCodeForDroplist(Map<String, Object> map)	throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> searchModelForDroplist(Map<String, Object> map)	throws ApplicationException, BusinessException;
	
	Map<String, Object> searchDenomForDroplist(Map<String, Object> map)	throws ApplicationException, BusinessException;
}
