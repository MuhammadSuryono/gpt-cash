package com.gpt.product.gpcash.corporate.corporateusergroup.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateUserGroupService extends CorporateAdminWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void saveCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String createdBy)
			throws ApplicationException, BusinessException;

	void updateCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String updatedBy)
			throws ApplicationException, BusinessException;

	void deleteCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String deletedBy)
			throws ApplicationException, BusinessException;

	CorporateUserGroupModel saveCorporateUserGroupAdmin(CorporateModel corporate, String roleCode, String createdBy)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateMenu(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchUserGroupByCorporateId(String corporateId) throws ApplicationException, BusinessException;

	void resetLimit(String parameter) throws ApplicationException, BusinessException;

	Map<String, Object> findDetailByUserGroupId(String corporateId, String userCode)
			throws ApplicationException, BusinessException;

	void deleteALLCorporateMenuByMenuList(String roleCode, List<String> menuList) throws Exception;

	Map<String, Object> findMenuForPendingTask(String corporateId, String userCode)
			throws ApplicationException, BusinessException;
}