package com.gpt.product.gpcash.corporate.transaction.inhouse.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.inhouse.model.InHouseTransferModel;

@Repository
public interface InHouseTransferRepository extends JpaRepository<InHouseTransferModel, String>, CashRepository<InHouseTransferModel>{

	@Query("select ih from InHouseTransferModel ih "
			+ "join fetch ih.corporate "
			+ "join fetch ih.service "
			+ "join fetch ih.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch ih.corporateUserGroup "
			+ "join fetch ih.application "
			+ "where ih.isProcessed = 'N' and ih.instructionMode = ?1 and ih.instructionDate between ?2 and ?3")	
	List<InHouseTransferModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select ih from InHouseTransferModel ih "
			+ "where ih.isProcessed = 'N' and ih.corporate.id = ?1 and ih.isFinalPayment = ?2 and ih.senderRefNo = ?3 ")	
	List<InHouseTransferModel> findTransactionForFinalPaymentFlag(String corporateId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select ih from InHouseTransferModel ih "
			+ "where ih.isProcessed = 'Y' and ih.corporate.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<InHouseTransferModel> findTransactionStatusLatest(String corporateId, String referenceNo);
	
	InHouseTransferModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}
