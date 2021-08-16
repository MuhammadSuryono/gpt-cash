package com.gpt.product.gpcash.corporate.corporateusermanagerforbank.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateUserManagerForBankSC {
	
	String menuCode = "MNU_GPCASH_IDM_USER_MANAGER_CORP";

	Map<String, Object> findByStillLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}
