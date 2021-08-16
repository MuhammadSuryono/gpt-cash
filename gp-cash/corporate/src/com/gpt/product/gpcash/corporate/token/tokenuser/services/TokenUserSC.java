package com.gpt.product.gpcash.corporate.token.tokenuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface TokenUserSC extends WorkflowService{
	
	String menuCode = "MNU_GPCASH_AUTH_DEVICE";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> unassignToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> unblockToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> unlockToken(Map<String, Object> map) throws ApplicationException, BusinessException;
}

