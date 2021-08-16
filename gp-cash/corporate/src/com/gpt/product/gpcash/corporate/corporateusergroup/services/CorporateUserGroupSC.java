package com.gpt.product.gpcash.corporate.corporateusergroup.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateUserGroupSC extends CorporateAdminWorkflowService{
	
	String menuCode = "MNU_GPCASH_F_USER_GROUP";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateLimitList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateMenu(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map) throws ApplicationException, BusinessException;

	void resetLimit(String parameter) throws ApplicationException, BusinessException;
}
