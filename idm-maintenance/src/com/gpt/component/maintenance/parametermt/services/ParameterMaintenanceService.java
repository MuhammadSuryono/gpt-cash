package com.gpt.component.maintenance.parametermt.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ParameterMaintenanceService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchModel(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	public Object getParameterMaintenanceByModelAndName(String modelCode, String name) throws ClassNotFoundException;
	
}
