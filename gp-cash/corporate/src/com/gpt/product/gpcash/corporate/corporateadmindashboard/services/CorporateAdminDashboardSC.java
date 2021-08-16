package com.gpt.product.gpcash.corporate.corporateadmindashboard.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateAdminDashboardSC {
	String menuCode = "MNU_GPCASH_F_ADMIN_DASHBOARD";

	Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateUserGroup(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> uploadAvatar(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> saveAvatar(Map<String, Object> map) throws ApplicationException, BusinessException;
}
