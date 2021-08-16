package com.gpt.product.gpcash.retail.transaction.inhouse.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.inhouse.model.CustomerInHouseTransferModel;

@Repository
public interface CustomerInHouseTransferRepository extends JpaRepository<CustomerInHouseTransferModel, String>, CashRepository<CustomerInHouseTransferModel>{

	@Query("select ih from CustomerInHouseTransferModel ih "
			+ "join fetch ih.customer "
			+ "join fetch ih.service "
			+ "join fetch ih.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch ih.application "
			+ "where ih.isProcessed = 'N' and ih.instructionMode = ?1 and ih.instructionDate between ?2 and ?3")	
	List<CustomerInHouseTransferModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select ih from CustomerInHouseTransferModel ih "
			+ "where ih.isProcessed = 'N' and ih.customer.id = ?1 and ih.isFinalPayment = ?2 and ih.senderRefNo = ?3 ")	
	List<CustomerInHouseTransferModel> findTransactionForFinalPaymentFlag(String customerId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select ih from CustomerInHouseTransferModel ih "
			+ "where ih.isProcessed = 'Y' and ih.customer.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<CustomerInHouseTransferModel> findTransactionStatusLatest(String customerId, String referenceNo);
}
