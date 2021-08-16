package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.model.SweepOutDetailModel;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.model.SweepOutModel;

@Repository
public interface SweepOutRepository extends JpaRepository<SweepOutModel, String>, CashRepository<SweepOutModel>{

	@Query("select sw from SweepOutModel sw "
			+ "join fetch sw.corporate "
			+ "join fetch sw.service "
			+ "join fetch sw.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch sw.corporateUserGroup "
			+ "join fetch sw.application "
			+ "where sw.isProcessed = 'N' and sw.instructionMode = ?1 and sw.instructionDate between ?2 and ?3")	
	List<SweepOutModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("from SweepOutDetailModel dtl "
			+ "where dtl.sweepOut.id = ?1")	
	Page<SweepOutDetailModel> searchSweepOutDetailById(String id, Pageable pageInfo) throws Exception;
	
	SweepOutModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}
