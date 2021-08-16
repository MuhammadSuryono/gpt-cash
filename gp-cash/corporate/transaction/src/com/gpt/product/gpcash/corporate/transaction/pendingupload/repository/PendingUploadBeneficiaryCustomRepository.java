package com.gpt.product.gpcash.corporate.transaction.pendingupload.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;

public interface PendingUploadBeneficiaryCustomRepository extends CashRepository<PendingUploadBeneficiaryModel> {
	
	Page<PendingUploadBeneficiaryModel> searchPendingUpload(Map<String,Object> map, Pageable pageInfo) throws Exception;
	
}

