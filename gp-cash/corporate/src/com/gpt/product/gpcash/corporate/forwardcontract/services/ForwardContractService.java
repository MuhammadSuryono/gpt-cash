package com.gpt.product.gpcash.corporate.forwardcontract.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.forwardcontract.model.ForwardContractModel;

@AutoDiscoveryImpl
public interface ForwardContractService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	ForwardContractModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception;

}
