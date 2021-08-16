package com.gpt.product.gpcash.retail.customeruserdashboard.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerUserDashboardService {

	Map<String, Object> getCountCustomerAccount(String customerId)
			throws ApplicationException, BusinessException;

}
