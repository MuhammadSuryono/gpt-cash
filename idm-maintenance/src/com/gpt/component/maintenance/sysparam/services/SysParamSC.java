package com.gpt.component.maintenance.sysparam.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface SysParamSC extends WorkflowService{
	
	String menuCode = "MNU_GPCASH_MT_SYS_PARAM";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getProductName(Map<String, Object> map) throws ApplicationException, BusinessException;
}
