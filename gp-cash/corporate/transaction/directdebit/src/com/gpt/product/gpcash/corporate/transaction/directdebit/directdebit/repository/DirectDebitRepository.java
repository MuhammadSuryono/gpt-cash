package com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model.DirectDebitDetailModel;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model.DirectDebitModel;

@Repository
public interface DirectDebitRepository extends JpaRepository<DirectDebitModel, String>, CashRepository<DirectDebitModel> {
	
	@Query("select p from DirectDebitModel p "
			+ "join fetch p.corporate "
			+ "join fetch p.service "
			+ "join fetch p.creditAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch p.corporateUserGroup "
			+ "join fetch p.application "
			+ "where p.isProcessed = 'N' and p.isHostProcessed = 'N' and p.instructionMode = ?1 and p.instructionDate between ?2 and ?3")
	List<DirectDebitModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);

	DirectDebitModel findByIdAndIsHostProcessed(String id, String isHostProcessed);
	
	@Query("from DirectDebitDetailModel dtl "
			+ "where dtl.directDebit.id = ?1 order by dtl.idx")	
	Page<DirectDebitDetailModel> searchDirectDebitDetailById(String id, Pageable pageInfo) throws Exception;		
	
	
}