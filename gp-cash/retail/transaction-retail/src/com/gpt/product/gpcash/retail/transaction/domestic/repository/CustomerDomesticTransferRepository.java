package com.gpt.product.gpcash.retail.transaction.domestic.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.domestic.model.CustomerDomesticTransferModel;

@Repository
public interface CustomerDomesticTransferRepository extends JpaRepository<CustomerDomesticTransferModel, String>, CashRepository<CustomerDomesticTransferModel>{

	@Query("select dom from CustomerDomesticTransferModel dom "
			+ "join fetch dom.customer "
			+ "join fetch dom.service "
			+ "join fetch dom.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch dom.application "
			+ "join fetch dom.lldBenResidenceCountry "
			+ "join fetch dom.lldBenCitizenCountry "
			+ "join fetch dom.benDomesticBankCode "
			+ "join fetch dom.benType "
			+ "where dom.isProcessed = 'N' and dom.instructionMode = ?1 and dom.instructionDate between ?2 and ?3")	
	List<CustomerDomesticTransferModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select dom from CustomerDomesticTransferModel dom "
			+ "where dom.isProcessed = 'N' and dom.customer.id = ?1 and dom.isFinalPayment = ?2 and dom.senderRefNo = ?3 ")	
	List<CustomerDomesticTransferModel> findTransactionForFinalPaymentFlag(String customerId, String finalPaymentFlag, String senderRefNo);
	
	@Query("select dom from CustomerDomesticTransferModel dom "
			+ "where dom.isProcessed = 'Y' and dom.customer.id = ?1 and referenceNo = ?2 order by instructionDate desc")	
	List<CustomerDomesticTransferModel> findTransactionStatusLatest(String customerId, String referenceNo);
	
	@Query("select dom from CustomerDomesticTransferModel dom "
			+ "where dom.isProcessed = 'Y' "
			+ "and dom.instructionMode = 'R' "
			+ "and dom.pendingTaskId = ?1 "
			+ "and dom.instructionDate between ?2 and ?3 "
			+ "order by instructionDate Asc")
	List<CustomerDomesticTransferModel> findRecurringTransactionForReport(String pendingTaskId, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select dom from CustomerDomesticTransferModel dom "
			+ "join fetch dom.customer "
			+ "join fetch dom.service "
			+ "join fetch dom.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch dom.application "
			+ "join fetch dom.benDomesticBankCode "
			+ "where dom.isProcessed = 'N' and dom.service.code = 'GPT_FTR_DOM_ONLINE' and dom.instructionMode = ?1 and dom.instructionDate between ?2 and ?3")	
	List<CustomerDomesticTransferModel> findOnlineTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	
}