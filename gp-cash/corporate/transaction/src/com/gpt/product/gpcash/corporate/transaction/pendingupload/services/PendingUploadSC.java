package com.gpt.product.gpcash.corporate.transaction.pendingupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface PendingUploadSC {
	
	Map<String, Object> submitBucket(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;	

	Map<String, Object> submitBucketBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUploadBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;

}
