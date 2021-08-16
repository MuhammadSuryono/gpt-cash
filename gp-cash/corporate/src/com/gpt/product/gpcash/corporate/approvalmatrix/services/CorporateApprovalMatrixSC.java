package com.gpt.product.gpcash.corporate.approvalmatrix.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateApprovalMatrixSC extends CorporateAdminWorkflowService {
	String menuCode = "MNU_GPCASH_F_APRV_MTRX";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchUserGroup(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchAuthorizedLimitScheme(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> getCurrency(Map<String, Object> map) throws ApplicationException, BusinessException;
}