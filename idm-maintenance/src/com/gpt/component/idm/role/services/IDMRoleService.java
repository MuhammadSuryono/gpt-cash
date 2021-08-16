package com.gpt.component.idm.role.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.idm.role.model.IDMRoleModel;

@AutoDiscoveryImpl
public interface IDMRoleService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveRole(IDMRoleModel idmRole, String createdBy) throws ApplicationException, BusinessException;
	
	void updateRole(IDMRoleModel idmRole, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteRole(IDMRoleModel idmRole, String deletedBy) throws ApplicationException, BusinessException;
}

