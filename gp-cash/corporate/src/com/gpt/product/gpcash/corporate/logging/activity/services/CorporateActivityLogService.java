package com.gpt.product.gpcash.corporate.logging.activity.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.logging.activity.valueobject.CorporateActivityLogVO;

@AutoDiscoveryImpl
public interface CorporateActivityLogService {
	void saveCorporateActivityLog(CorporateActivityLogVO vo) throws Exception;

	Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getNonFinancialActvity(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException;
}
