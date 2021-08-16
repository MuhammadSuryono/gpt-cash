package com.gpt.product.gpcash.corporate.transaction.pendingupload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryErrorModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryModel;


@Repository
public interface PendingUploadBeneficiaryRepository extends JpaRepository<PendingUploadBeneficiaryModel, String>, PendingUploadBeneficiaryCustomRepository {
	
	@Query("from PendingUploadBeneficiaryModel pu "
			+ "where pu.id = ?1 "
			+ "and pu.status = 'COMPLETED' "
			+ "and pu.deleteFlag = 'N' "
			+ "and pu.isProcessed = 'N' ")
	PendingUploadBeneficiaryModel detailPendingUploadById(String id);
	
	@Query("from PendingUploadBeneficiaryModel pu "
			+ "where pu.id = ?1 "
			+ "and pu.status = 'COMPLETED' "
			+ "and pu.deleteFlag = 'N' "
			+ "and pu.isProcessed = 'N' "
			+ "and pu.totalError = 0 ")	
	PendingUploadBeneficiaryModel detailPendingUploadByIdValidOnly(String id);
		
	PendingUploadBeneficiaryModel findByIdAndIsProcessed(String id, String isProcessed);
	
	PendingUploadBeneficiaryModel findByIdAndIsProcessedAndDeleteFlag(String id, String isProcessed, String deleteFlag);	
	
	@Query("from PendingUploadBeneficiaryDetailModel pud "
			+ "where pud.parent.id = ?1 ")	
	Page<PendingUploadBeneficiaryDetailModel> searchPendingUploadDetail(String id, Pageable pageInfo) throws Exception;		

	@Query("from PendingUploadBeneficiaryErrorModel pue "
			+ "where pue.parent.id = ?1 order by pue.line")	
	Page<PendingUploadBeneficiaryErrorModel> searchPendingUploadError(String id, Pageable pageInfo) throws Exception;	
	
	PendingUploadBeneficiaryModel findByPendingTaskId(String pendingTaskId);
	
	@Query("from PendingUploadBeneficiaryDetailModel pud "
			+ "where pud.parent.id <> ?1 "
			+ "and pud.parent.corporate.id = ?2 "
			+ "and pud.parent.isProcessed = 'Y' "
			+ "and pud.parent.isError='N' "
			+ "and pud.finalizeFlag = 'Y' ")
	List<PendingUploadBeneficiaryDetailModel> findTransactionForFinalPaymentFlag(String id, String corporateId);
	
}
