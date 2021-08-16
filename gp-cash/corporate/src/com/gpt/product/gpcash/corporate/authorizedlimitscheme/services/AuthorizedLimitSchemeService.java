package com.gpt.product.gpcash.corporate.authorizedlimitscheme.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface AuthorizedLimitSchemeService extends CorporateAdminWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String createdBy) throws ApplicationException, BusinessException;
	
	void updateAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String deletedBy) throws ApplicationException, BusinessException;

	Map<String, Object> searchAuthorizedLimitSchemeByCorporateId(String corporateId)
			throws ApplicationException, BusinessException;
}