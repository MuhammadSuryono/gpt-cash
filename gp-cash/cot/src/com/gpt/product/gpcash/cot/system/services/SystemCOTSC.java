package com.gpt.product.gpcash.cot.system.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface SystemCOTSC extends WorkflowService{
	
	String menuCode = "MNU_GPCASH_COT";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateCOT(Map<String, Object> map) throws Exception;
}
