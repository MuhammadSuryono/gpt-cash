package com.gpt.component.idm.loginhistory.services;

import java.util.List;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.idm.loginhistory.model.IDMLoginHistoryModel;

@AutoDiscoveryImpl
public interface IDMLoginHistoryService {
	IDMLoginHistoryModel saveLoginHistory(String userCode, String applicationCode, String ipAddress)
			throws ApplicationException, BusinessException;

	void updateLogoutLoginHistory(String loginHistoryId) throws ApplicationException, BusinessException;

	void updateLogoutLoginHistories(List<String> loginHistoryIds) throws ApplicationException, BusinessException;
	
	IDMLoginHistoryModel getLoginHistoryById(String loginHistoryId) throws ApplicationException, BusinessException;
}
