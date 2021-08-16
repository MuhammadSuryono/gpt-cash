package com.gpt.product.gpcash.corporate.transaction.pendingupload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;

@AutoDiscoveryImpl
public interface PendingUploadService {
	
	Map<String, Object> submitToBucket(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingUploadByIdValidOnly(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	PendingUploadModel save(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	PendingUploadModel update(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailCreatedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException;	

	Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	// Beneficiary Upload
	Map<String, Object> submitBeneficiaryToBucket(Map<String, Object> map) throws ApplicationException, BusinessException;
	PendingUploadBeneficiaryModel saveBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	PendingUploadBeneficiaryModel updateBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchPendingUploadBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchBeneficiaryUploadById(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> deleteBeneficiaryUpload(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchBeneficiaryUploadByIdValidOnly(Map<String, Object> map) throws ApplicationException, BusinessException;
}
