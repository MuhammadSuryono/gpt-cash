package com.gpt.product.gpcash.corporate.authorizedlimitscheme.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface AuthorizedLimitSchemeSC extends CorporateAdminWorkflowService{
	
	String menuCode = "MNU_GPCASH_F_AUTH_LMT_SCHEME";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
}