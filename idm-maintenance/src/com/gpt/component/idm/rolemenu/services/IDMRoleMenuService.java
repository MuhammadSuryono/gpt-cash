package com.gpt.component.idm.rolemenu.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;

@AutoDiscoveryImpl
public interface IDMRoleMenuService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void saveRoleMenu(IDMRoleModel idmRole, List<IDMMenuModel> menuList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateRoleMenu(IDMRoleModel idmRole, List<IDMMenuModel> menuList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteRoleMenu(IDMRoleModel idmRole) throws ApplicationException, BusinessException;
	
	List<IDMRoleMenuModel> searchRoleMenu(String roleCode) throws ApplicationException, BusinessException;
	
	List<Map<String, Object>> searchRoleMenuGetMap(String roleCode, boolean isIncludeParent) throws ApplicationException, BusinessException;

	void deleteRoleMenuNewTrx(IDMRoleModel idmRole) throws ApplicationException, BusinessException;
}
