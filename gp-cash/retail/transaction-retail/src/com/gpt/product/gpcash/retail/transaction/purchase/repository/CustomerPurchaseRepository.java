package com.gpt.product.gpcash.retail.transaction.purchase.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.purchase.model.CustomerPurchaseModel;

@Repository
public interface CustomerPurchaseRepository extends JpaRepository<CustomerPurchaseModel, String>, CashRepository<CustomerPurchaseModel>{

	@Query("select bp from CustomerPurchaseModel bp "
			+ "join fetch bp.customer "
			+ "join fetch bp.service "
			+ "join fetch bp.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch bp.application "
			+ "where bp.isProcessed = 'N' and bp.instructionMode = ?1 and bp.instructionDate between ?2 and ?3")		
	List<CustomerPurchaseModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select bp from CustomerPurchaseModel bp "
			+ "where bp.isProcessed = 'N' and bp.customer.id = ?1 and bp.isFinalPayment = ?2 and bp.senderRefNo = ?3 ")	
	List<CustomerPurchaseModel> findTransactionForFinalPaymentFlag(String customerId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select bp from CustomerPurchaseModel bp "
			+ "where bp.isProcessed = 'Y' and bp.customer.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<CustomerPurchaseModel> findTransactionStatusLatest(String customerId, String referenceNo);
	
	CustomerPurchaseModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}
