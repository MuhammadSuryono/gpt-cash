package com.gpt.product.gpcash.corporate.corporatetdspecialrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.corporatetdspecialrate.model.TDSpecialRateModel;

@AutoDiscoveryImpl
public interface TDSpecialRateService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	TDSpecialRateModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception;

}
