package com.gpt.component.idm.userlock.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMUserLockService {
	void unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void unlockUser(String userCode) throws ApplicationException, BusinessException;

	void lockUser(String userCode) throws ApplicationException, BusinessException;
}
