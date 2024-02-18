package com.gpt.product.gpcash.retail.customerusermanagerforbank.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerUserManagerForBankSC {
	
	String menuCode = "MNU_GPCASH_IDM_USER_MANAGER_CUST";

	Map<String, Object> findByStillLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCustomerForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}
