package com.gpt.product.gpcash.corporate.corporateusermanager.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateUserManagerSC {
	
	String menuCode = "MNU_GPCASH_F_USER_MANAGER";

	Map<String, Object> findByStillLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> findByLockUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException;
}
