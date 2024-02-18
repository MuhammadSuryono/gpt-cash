package com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.model.CustomerTimeDepositSpecialRateModel;

@AutoDiscoveryImpl
public interface CustomerTimeDepositSpecialRateService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	CustomerTimeDepositSpecialRateModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception;

}
