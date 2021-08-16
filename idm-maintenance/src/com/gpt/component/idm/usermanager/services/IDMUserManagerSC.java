package com.gpt.component.idm.usermanager.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMUserManagerSC {
	
	String menuCode = "MNU_GPCASH_IDM_USER_MANAGER";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException;
}
