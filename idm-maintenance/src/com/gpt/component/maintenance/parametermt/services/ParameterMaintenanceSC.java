package com.gpt.component.maintenance.parametermt.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ParameterMaintenanceSC extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_MT_PARAMETER";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchModel(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchModelForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
}
