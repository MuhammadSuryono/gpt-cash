package com.gpt.product.gpcash.corporate.transaction.beneficiaryupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface BeneficiaryListUploadSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_FUND_BENEFICIARY_UPLOAD";
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getFileFormats(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException;	

	Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;


	
}
