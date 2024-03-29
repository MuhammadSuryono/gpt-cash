package com.gpt.product.gpcash.servicepackage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ServicePackageSC extends WorkflowService{
	
	String menuCode = "MNU_GPCASH_PRO_SRVC_PC";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCharge(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchLimit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException;

}
