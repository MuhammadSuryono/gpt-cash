package com.gpt.component.maintenance.errormapping.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;

@AutoDiscoveryImpl
public interface ErrorMappingService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveErrorMapping(ErrorMappingModel errorMapping, String createdBy) throws ApplicationException, BusinessException;
	
	void updateErrorMapping(ErrorMappingModel errorMapping, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteErrorMapping(ErrorMappingModel errorMapping, String deletedBy) throws ApplicationException, BusinessException;
}
