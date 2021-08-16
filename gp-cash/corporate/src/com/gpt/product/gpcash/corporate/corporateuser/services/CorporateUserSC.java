package com.gpt.product.gpcash.corporate.corporateuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateUserSC extends CorporateAdminWorkflowService {
	String menuCode = "MNU_GPCASH_F_USER_MAINTENANCE";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchWFRole(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchAuthorizedLimitScheme(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchUserGroup(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTokenUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDeleteList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> updateUserNotificationFlag(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchTokenUserForEditUser(Map<String, Object> map)
			throws ApplicationException, BusinessException;
}
