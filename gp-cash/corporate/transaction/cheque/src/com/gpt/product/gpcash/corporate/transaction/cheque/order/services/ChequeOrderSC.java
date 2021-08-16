package com.gpt.product.gpcash.corporate.transaction.cheque.order.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface ChequeOrderSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_CHQ_ORDER";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCityForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBranchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getLocalCurrency(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchChequePagesForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}