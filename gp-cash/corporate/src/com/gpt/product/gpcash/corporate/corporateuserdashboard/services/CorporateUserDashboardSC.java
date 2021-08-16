package com.gpt.product.gpcash.corporate.corporateuserdashboard.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateUserDashboardSC {
	String menuCode = "MNU_GPCASH_F_USER_DASHBOARD";

	Map<String, Object> searchCorporateUserGroupAccount(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCountPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> countTotalCreatedTrx(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> countTotalExecutedTrx(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findLimitUsage(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findCOT(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> updateUserNotificationFlag(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> uploadAvatar(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> saveAvatar(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getPromo(Map<String, Object> map) throws ApplicationException, BusinessException;
}
