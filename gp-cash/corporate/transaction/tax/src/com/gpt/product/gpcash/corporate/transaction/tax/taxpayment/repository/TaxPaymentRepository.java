package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.model.TaxPaymentModel;

@Repository
public interface TaxPaymentRepository extends JpaRepository<TaxPaymentModel, String>, CashRepository<TaxPaymentModel>{

	@Query("select p from TaxPaymentModel p "
			+ "join fetch p.corporate "
			+ "join fetch p.service "
			+ "join fetch p.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch p.corporateUserGroup "
			+ "join fetch p.application "
			+ "where p.isProcessed = 'N' and p.instructionMode = ?1 and p.instructionDate between ?2 and ?3")	
	List<TaxPaymentModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select p from TaxPaymentModel p "
			+ "where p.isProcessed = 'N' and p.corporate.id = ?1 and p.isFinalPayment = ?2 and p.senderRefNo = ?3 ")	
	List<TaxPaymentModel> findTransactionForFinalPaymentFlag(String corporateId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select p from TaxPaymentModel p "
			+ "where p.isProcessed = 'Y' and p.corporate.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<TaxPaymentModel> findTransactionStatusLatest(String corporateId, String referenceNo);
	
	@Query("select p from TaxPaymentModel p where p.isProcessed = 'Y' and p.isError = 'N' and p.ntp is null order by p.corporate.id, p.paymentDate, p.referenceNo")	
	Page<TaxPaymentModel> findPendingNTP(Pageable pageInfo);
	
	@Query("select p.ntp from TaxPaymentModel p where p.referenceNo = ?1")	
	String findNTPByRefNo(String refNo);
	
	TaxPaymentModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}