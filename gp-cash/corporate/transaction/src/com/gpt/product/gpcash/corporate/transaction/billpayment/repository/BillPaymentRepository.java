package com.gpt.product.gpcash.corporate.transaction.billpayment.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.billpayment.model.BillPaymentModel;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPaymentModel, String>, CashRepository<BillPaymentModel>{

	@Query("select bp from BillPaymentModel bp "
			+ "join fetch bp.corporate "
			+ "join fetch bp.service "
			+ "join fetch bp.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch bp.corporateUserGroup "
			+ "join fetch bp.application "
			+ "where bp.isProcessed = 'N' and bp.instructionMode = ?1 and bp.instructionDate between ?2 and ?3")		
	List<BillPaymentModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select bp from BillPaymentModel bp "
			+ "where bp.isProcessed = 'N' and bp.corporate.id = ?1 and bp.isFinalPayment = ?2 and bp.senderRefNo = ?3 ")	
	List<BillPaymentModel> findTransactionForFinalPaymentFlag(String corporateId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select bp from BillPaymentModel bp "
			+ "where bp.isProcessed = 'Y' and bp.corporate.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<BillPaymentModel> findTransactionStatusLatest(String corporateId, String referenceNo);
	
	BillPaymentModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}
