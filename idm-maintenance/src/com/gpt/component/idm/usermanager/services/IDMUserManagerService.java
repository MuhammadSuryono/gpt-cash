package com.gpt.component.idm.usermanager.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMUserManagerService{
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException;

}