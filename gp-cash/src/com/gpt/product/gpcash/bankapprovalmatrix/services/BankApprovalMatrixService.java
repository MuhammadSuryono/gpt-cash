package com.gpt.product.gpcash.bankapprovalmatrix.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixDetailModel;

@AutoDiscoveryImpl
public interface BankApprovalMatrixService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBankApprovalMatrixDetail(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getBankApprovalMatrixMenu(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	List<BankApprovalMatrixDetailModel> getBankApprovalMatrixDetailForWorkflow(String menuCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> getApprovaLevelforDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}

