package com.gpt.product.gpcash.corporate.transaction.va.accountlistupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;

@AutoDiscoveryImpl
public interface VAAccountListUploadSC extends CorporateUserWorkflowService {
	String menuCode = PendingUploadConstants.MNU_GPCASH_F_CORP_VA_UPLOAD;
	
	Map<String, Object> searchMainAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException;	

	Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getFileFormats(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitBucket(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchProductCode(Map<String, Object> map) throws ApplicationException, BusinessException;
}