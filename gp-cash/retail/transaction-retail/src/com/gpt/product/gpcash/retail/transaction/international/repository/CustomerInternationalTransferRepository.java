package com.gpt.product.gpcash.retail.transaction.international.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.international.model.CustomerInternationalTransferModel;

@Repository
public interface CustomerInternationalTransferRepository extends JpaRepository<CustomerInternationalTransferModel, String>, CashRepository<CustomerInternationalTransferModel>{

	@Query("select int from CustomerInternationalTransferModel int "
			+ "join fetch int.customer "
			+ "join fetch int.service "
			+ "join fetch int.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch int.application "
			+ "join fetch int.lldBenResidenceCountry "
			+ "join fetch int.lldBenCitizenCountry "
			+ "join fetch int.benInternationalBankCode "
			+ "where int.isProcessed = 'N' and int.instructionMode = ?1 and int.instructionDate between ?2 and ?3")	
	List<CustomerInternationalTransferModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select int from CustomerInternationalTransferModel int "
			+ "where int.isProcessed = 'N' and int.customer.id = ?1 and int.isFinalPayment = ?2 and int.senderRefNo = ?3 ")	
	List<CustomerInternationalTransferModel> findTransactionForFinalPaymentFlag(String customerId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select int from CustomerInternationalTransferModel int "
			+ "where int.isProcessed = 'Y' and int.customer.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<CustomerInternationalTransferModel> findTransactionStatusLatest(String customerId, String referenceNo);
}