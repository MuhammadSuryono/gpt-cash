package com.gpt.product.gpcash.corporate.corporatetdspecialrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface TDSpecialRateSC extends WorkflowService {

	String menuCode = "MNU_GPCASH_MT_TD_SPECIAL_RATE";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
