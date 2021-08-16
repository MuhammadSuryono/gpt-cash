package com.gpt.product.gpcash.corporate.corporateadmindashboard.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateAdminDashboardService {

	Map<String, Object> searchCorporateAccountGroup(String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateUserGroup(String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccount(String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateUser(String corporateId) throws ApplicationException, BusinessException;

}
