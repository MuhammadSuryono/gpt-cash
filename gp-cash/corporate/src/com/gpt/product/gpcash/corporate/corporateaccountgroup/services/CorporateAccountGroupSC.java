package com.gpt.product.gpcash.corporate.corporateaccountgroup.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateAccountGroupSC extends CorporateAdminWorkflowService {
	String menuCode = "MNU_GPCASH_F_ACCT_GROUP";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDeleteAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
}
