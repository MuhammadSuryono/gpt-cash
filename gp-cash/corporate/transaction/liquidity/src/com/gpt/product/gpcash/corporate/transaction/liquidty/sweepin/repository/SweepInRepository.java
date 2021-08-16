package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepin.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepin.model.SweepInDetailModel;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepin.model.SweepInModel;

@Repository
public interface SweepInRepository extends JpaRepository<SweepInModel, String>, CashRepository<SweepInModel>{

	@Query("select sw from SweepInModel sw "
			+ "join fetch sw.corporate "
			+ "join fetch sw.service "
			+ "join fetch sw.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch sw.corporateUserGroup "
			+ "join fetch sw.application "
			+ "where sw.isProcessed = 'N' and sw.instructionMode = ?1 and sw.instructionDate between ?2 and ?3")	
	List<SweepInModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("select sw from SweepInModel sw "
			+ "join fetch sw.corporate "
			+ "join fetch sw.service "
			+ "join fetch sw.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch sw.corporateUserGroup "
			+ "join fetch sw.application "
			+ "where sw.isProcessed = 'Y' and sw.instructionDate between ?1 and ?2 and sw.isAutoSweepBack = 'Y'")	
	List<SweepInModel> findTransactionForSweepBack(Timestamp startInstructionDate, Timestamp endInstructionDate);
	
	@Query("from SweepInDetailModel dtl where id = ?1")
	SweepInDetailModel findDetailById(String detailId);
	
	@Query("from SweepInDetailModel dtl "
			+ "where dtl.sweepIn.id = ?1")	
	Page<SweepInDetailModel> searchSweepInDetailById(String id, Pageable pageInfo) throws Exception;		
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Modifying
	@Query("update SweepInDetailModel detail set sweepBackStatus = ?2, sweepBackIsError = ?3, sweepBackErrorCode = ?4, sweepBackDate = ?5  where detail.id = ?1")
	void updateSweepBackDetailStatus(String detailId, String status, String isError, String errorCode, Date sweepBackDate);
	
	SweepInModel findByPendingTaskIdAndIsProcessed(String pendingTaskId, String isProcessed);
}
