package com.gpt.product.gpcash.retail.customerlimit.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CustomerLimitSC  extends WorkflowService {
	
	String menuCode = "MNU_R_GPCASH_CUST_LMT_PC_DTL";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCustomer(Map<String, Object> map) throws ApplicationException, BusinessException;

	void resetLimit(String parameter) throws ApplicationException, BusinessException;

}
