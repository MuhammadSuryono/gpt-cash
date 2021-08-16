package com.gpt.product.gpcash.retail.logging.activity.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.logging.activity.valueobject.CustomerActivityLogVO;

@AutoDiscoveryImpl
public interface CustomerActivityLogService {
	void saveCustomerActivityLog(CustomerActivityLogVO vo) throws Exception;

	Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getNonFinancialActvity(Map<String, Object> map) throws ApplicationException, BusinessException;
}
