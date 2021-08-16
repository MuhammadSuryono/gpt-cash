package com.gpt.product.gpcash.corporate.corporatelimit.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CorporateLimitSC  extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_CORP_LMT_PC_DTL";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	void resetLimit(String parameter) throws ApplicationException, BusinessException;

}
