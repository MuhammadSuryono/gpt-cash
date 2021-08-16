package com.gpt.component.idm.userapp.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface IDMUserAppSC extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_IDM_USER_APP";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

}
