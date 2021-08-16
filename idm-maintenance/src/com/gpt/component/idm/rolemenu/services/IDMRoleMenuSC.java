package com.gpt.component.idm.rolemenu.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface IDMRoleMenuSC extends WorkflowService {

	String menuCode = "MNU_GPCASH_IDM_ROLE_MENU";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException;
}
