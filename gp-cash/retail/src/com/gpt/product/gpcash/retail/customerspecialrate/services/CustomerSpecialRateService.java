package com.gpt.product.gpcash.retail.customerspecialrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.customerspecialrate.model.CustomerSpecialRateModel;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CustomerSpecialRateService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	CustomerSpecialRateModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception;

}
