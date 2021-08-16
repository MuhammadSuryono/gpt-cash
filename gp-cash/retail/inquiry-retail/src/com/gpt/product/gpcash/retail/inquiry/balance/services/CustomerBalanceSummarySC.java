package com.gpt.product.gpcash.retail.inquiry.balance.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerBalanceSummarySC {
	
	String menuCode = "MNU_R_GPCASH_F_BAL_SUMMARY";

	Map<String, Object> singleAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> multiAccountByBranch(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> multiAccountByAccountType(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCustomerAccountForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchAccountType(Map<String, Object> map) throws ApplicationException, BusinessException;
}
