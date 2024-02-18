package com.gpt.product.gpcash.retail.transaction.timedeposit.withdrawal.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;

@AutoDiscoveryImpl
public interface CustomerTimeDepositWithdrawService extends CustomerUserWorkflowService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailTimeDeposit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTimeDepositAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
}