package com.gpt.component.idm.login.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMLoginSC {
	Map<String, Object> login(Map<String, Object> map) throws ApplicationException, BusinessException;

	void logout(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getProfiles(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
