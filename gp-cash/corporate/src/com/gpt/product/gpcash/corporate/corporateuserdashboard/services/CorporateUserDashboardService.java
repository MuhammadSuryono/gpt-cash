package com.gpt.product.gpcash.corporate.corporateuserdashboard.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateUserDashboardService {

	Map<String, Object> getCountCorporateIdAndGroupId(String corporateId, String userCode)
			throws ApplicationException, BusinessException;

}
