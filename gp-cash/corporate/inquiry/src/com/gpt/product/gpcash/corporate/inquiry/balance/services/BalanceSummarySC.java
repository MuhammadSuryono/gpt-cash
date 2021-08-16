package com.gpt.product.gpcash.corporate.inquiry.balance.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface BalanceSummarySC {
	
	String menuCode = "MNU_GPCASH_F_BAL_SUMMARY";

	Map<String, Object> singleAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> multiAccountByBranch(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> multiAccountByAccountType(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchAccountType(Map<String, Object> map) throws ApplicationException, BusinessException;
}
