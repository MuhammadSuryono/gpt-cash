package com.gpt.product.gpcash.cot.system.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.cot.system.model.SystemCOTModel;

@AutoDiscoveryImpl
public interface SystemCOTService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void updateSystemCOT(SystemCOTModel systemCOT, String updatedBy) throws ApplicationException, BusinessException;

	void validateSystemCOT(String code, String applicationCode) throws Exception;

	Map<String, Object> findByApplicationCodeSortByEndTime(String applicationCode)
			throws ApplicationException, BusinessException;

	void validateSystemCOTWithSessionTime(String code, String applicationCode, String sessionTime) throws Exception;
	
	boolean validateSystemCOTForChecking(String code, String applicationCode) throws Exception;
}
