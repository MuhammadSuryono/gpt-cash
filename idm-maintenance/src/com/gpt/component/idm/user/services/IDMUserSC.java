package com.gpt.component.idm.user.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface IDMUserSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_IDM_USER";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchRole(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> inactivateUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> activateUser(Map<String, Object> map) throws ApplicationException, BusinessException;
}
