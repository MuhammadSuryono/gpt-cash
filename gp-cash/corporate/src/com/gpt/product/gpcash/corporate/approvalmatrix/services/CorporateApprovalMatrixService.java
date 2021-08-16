package com.gpt.product.gpcash.corporate.approvalmatrix.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixDetailModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;

@AutoDiscoveryImpl
public interface CorporateApprovalMatrixService extends CorporateAdminWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<CorporateApprovalMatrixDetailModel> getApprovalMatrixDetailForWorkflow(String menuCode, String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> getCurrency() throws ApplicationException, BusinessException;

}