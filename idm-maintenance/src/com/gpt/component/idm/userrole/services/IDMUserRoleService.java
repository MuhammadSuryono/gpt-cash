package com.gpt.component.idm.userrole.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.idm.user.model.IDMUserModel;

@AutoDiscoveryImpl
public interface IDMUserRoleService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	public void saveUserRole(IDMUserModel idmUser, String roleCode, String createdBy);
	
	void updateUserRole(IDMUserModel idmUser, List<Map<String, Object>> roleCodeList, String createdBy);
}

