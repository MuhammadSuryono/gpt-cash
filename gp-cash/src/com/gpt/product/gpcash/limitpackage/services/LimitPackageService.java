package com.gpt.product.gpcash.limitpackage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface LimitPackageService  extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchServiceCurrencyMatrixTRX(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchLimitPackageDetailByCode(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchSpesific(String code, String applicationCode) throws ApplicationException;
	
}
