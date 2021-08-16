package com.gpt.product.gpcash.corporate.transaction.international.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.international.model.InternationalTransferModel;

@Repository
public interface InternationalTransferRepository extends JpaRepository<InternationalTransferModel, String>, CashRepository<InternationalTransferModel>{

	@Query("select int from InternationalTransferModel int "
			+ "join fetch int.corporate "
			+ "join fetch int.service "
			+ "join fetch int.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch int.corporateUserGroup "
			+ "join fetch int.application "
			+ "join fetch int.lldBenResidenceCountry "
			+ "join fetch int.lldBenCitizenCountry "
			+ "join fetch int.benInternationalBankCode "
			+ "where int.isProcessed = 'N' and int.instructionMode = ?1 and int.instructionDate between ?2 and ?3")	
	List<InternationalTransferModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select int from InternationalTransferModel int "
			+ "where int.isProcessed = 'N' and int.corporate.id = ?1 and int.isFinalPayment = ?2 and int.senderRefNo = ?3 ")	
	List<InternationalTransferModel> findTransactionForFinalPaymentFlag(String corporateId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select int from InternationalTransferModel int "
			+ "where int.isProcessed = 'Y' and int.corporate.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<InternationalTransferModel> findTransactionStatusLatest(String corporateId, String referenceNo);
}