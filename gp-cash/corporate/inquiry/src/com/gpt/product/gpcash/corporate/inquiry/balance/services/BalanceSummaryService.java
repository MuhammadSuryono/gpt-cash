package com.gpt.product.gpcash.corporate.inquiry.balance.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.account.model.AccountModel;

@AutoDiscoveryImpl
public interface BalanceSummaryService {
	
	Map<String, Object> singleAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> multiAccountByBranch(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> multiAccountByAccountType(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> singleAccountNonSummary(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> doCheckBalance(AccountModel account) throws Exception;
	
}
