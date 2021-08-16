package com.gpt.product.gpcash.retail.customeraccount.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CustomerAccountSC extends WorkflowService {
	String menuCode = "MNU_R_GPCASH_CUST_ACCT";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByCIF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCustomerId(Map<String, Object> map) throws ApplicationException, BusinessException;
}
