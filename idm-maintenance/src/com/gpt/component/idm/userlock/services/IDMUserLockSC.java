package com.gpt.component.idm.userlock.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMUserLockSC {
	
	String menuCode = "MNU_GPCASH_IDM_USER_LOCK";
	
	void unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
}
