package com.gpt.product.gpcash.limitupdate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface LimitUpdateSC  extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_PRO_LMT_UPDATE";

	Map<String, Object> searchAll(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchSpesific(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchAllLimitPackage(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCurrencyForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

}