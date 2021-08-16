package com.gpt.product.gpcash.corporate.transaction.beneficiaryupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.payroll.model.PayrollModel;

@AutoDiscoveryImpl
public interface BeneficiaryListUploadService extends CorporateUserWorkflowService {
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
		
}
