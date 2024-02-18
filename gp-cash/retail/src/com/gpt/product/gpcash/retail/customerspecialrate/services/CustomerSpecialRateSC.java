package com.gpt.product.gpcash.retail.customerspecialrate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;;

@AutoDiscoveryImpl
public interface CustomerSpecialRateSC extends WorkflowService {

	String menuCode = "MNU_R_GPCASH_MT_SPECIAL_RATE";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
