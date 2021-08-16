package com.gpt.product.gpcash.corporate.transaction.pendingupload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadErrorModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;


@Repository
public interface PendingUploadRepository extends JpaRepository<PendingUploadModel, String>, PendingUploadCustomRepository {
	
	@Query("from PendingUploadModel pu "
			+ "where pu.id = ?1 "
			+ "and pu.status = 'COMPLETED' "
			+ "and pu.deleteFlag = 'N' "
			+ "and pu.isProcessed = 'N' ")
	PendingUploadModel detailPendingUploadById(String id);
	
	@Query("from PendingUploadModel pu "
			+ "where pu.id = ?1 "
			+ "and pu.status = 'COMPLETED' "
			+ "and pu.deleteFlag = 'N' "
			+ "and pu.isProcessed = 'N' "
			+ "and pu.totalError = 0 ")	
	PendingUploadModel detailPendingUploadByIdValidOnly(String id);
		
	PendingUploadModel findByIdAndIsProcessed(String id, String isProcessed);
	
	PendingUploadModel findByIdAndIsProcessedAndDeleteFlag(String id, String isProcessed, String deleteFlag);	
	
	@Query("from PendingUploadDetailModel pud "
			+ "where pud.parent.id = ?1 ")	
	Page<PendingUploadDetailModel> searchPendingUploadDetail(String id, Pageable pageInfo) throws Exception;		

	@Query("from PendingUploadErrorModel pue "
			+ "where pue.parent.id = ?1 order by pue.line")	
	Page<PendingUploadErrorModel> searchPendingUploadError(String id, Pageable pageInfo) throws Exception;	
	
	PendingUploadModel findByPendingTaskId(String pendingTaskId);
	
	@Query("from PendingUploadDetailModel pud "
			+ "where pud.parent.id <> ?1 "
			+ "and pud.parent.corporate.id = ?2 "
			+ "and pud.parent.isProcessed = 'Y' "
			+ "and pud.parent.isError='N' "
			+ "and pud.finalizeFlag = 'Y' ")
	List<PendingUploadDetailModel> findTransactionForFinalPaymentFlag(String id, String corporateId);
	
}
