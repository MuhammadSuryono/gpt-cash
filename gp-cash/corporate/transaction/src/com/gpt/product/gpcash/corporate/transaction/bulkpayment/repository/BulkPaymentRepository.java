package com.gpt.product.gpcash.corporate.transaction.bulkpayment.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.model.BulkPaymentDetailModel;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.model.BulkPaymentModel;
@Repository
public interface BulkPaymentRepository extends JpaRepository<BulkPaymentModel, String>, CashRepository<BulkPaymentModel> {
	
	@Query("select p from BulkPaymentModel p "
			+ "join fetch p.corporate "
			+ "join fetch p.service "
			+ "join fetch p.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch p.corporateUserGroup "
			+ "join fetch p.application "
			+ "where p.isProcessed = 'N' and p.isHostProcessed = 'N' and p.instructionMode = ?1 and p.instructionDate between ?2 and ?3")
	List<BulkPaymentModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);

	BulkPaymentModel findByIdAndIsHostProcessed(String id, String isHostProcessed);
	
	@Query("from BulkPaymentDetailModel dtl "
			+ "where dtl.bulkPayment.id = ?1 order by dtl.idx")	
	Page<BulkPaymentDetailModel> searchBulkPaymentDetailById(String id, Pageable pageInfo) throws Exception;
	
	BulkPaymentModel findByIdAndIsProcessed(String id, String isProcessed);
	
	
}
