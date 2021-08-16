package com.gpt.product.gpcash.corporate.transaction.va.accountlistupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface VAAccountListUploadService extends CorporateUserWorkflowService {
	
	Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitBucket(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;
}