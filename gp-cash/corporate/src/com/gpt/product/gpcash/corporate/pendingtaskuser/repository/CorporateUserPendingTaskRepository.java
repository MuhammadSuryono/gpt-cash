package com.gpt.product.gpcash.corporate.pendingtaskuser.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;

@Repository
public interface CorporateUserPendingTaskRepository extends JpaRepository<CorporateUserPendingTaskModel, String>, CorporateUserPendingTaskCustomRepository {
	
	@Modifying(clearAutomatically = true)
	@Query("update CorporateUserPendingTaskModel p set p.activityDate = ?2, p.activityBy = ?3, p.trxStatus = ?4 where p.id = ?1")
	void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy, TransactionStatus trxStatus);

	@Modifying
	@Query("update CorporateUserPendingTaskModel p set p.status = ?2, p.activityDate = ?3, p.activityBy = ?4, p.trxStatus = ?5 where p.id = ?1")
	void updatePendingTask(String pendingTaskId, String status, Timestamp activityDate, String activityBy, TransactionStatus trxStatus);
	
	@Query("from CorporateUserPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.referenceNo = ?1")
	CorporateUserPendingTaskModel findByReferenceNo(String referenceNo);

	@Query("from CorporateUserPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.id = ?1")
	CorporateUserPendingTaskModel findById(String id);
	
	@Query("from CorporateUserPendingTaskModel p where p.referenceNo = ?1 and p.status = 'PENDING'")
	CorporateUserPendingTaskModel findByReferenceNoAndStatusIsPending(String referenceNo);
	
	List<CorporateUserPendingTaskModel> findByUniqueKeyAndStatusAndMenuCodeAndCorporateId(String uniqueKey, String status, String menuCode, String corporateId);
	
	List<CorporateUserPendingTaskModel> findByUniqueKeyContainingAndStatusAndMenuCodeAndCorporateId(String uniqueKey, String status, String menuCode, String corporateId);

	@Modifying
	@Query("update CorporateUserPendingTaskModel p set p.status = ?2 where p.id = ?1")
	void updateStatusPendingTask(String pendingTaskID, String status) throws Exception;

	@Query("from CorporateUserPendingTaskModel where instructionDate between ?1 and ?2 and instructionMode in (?3) and menu.menuType.code in ('T','M') and status = 'PENDING'")
	List<CorporateUserPendingTaskModel> findExpiredPendingTask(Timestamp startExpiryDate, Timestamp endExpiryDate, List<String> instructionMode);

	@Query("select count(totalDebitedEquivalentAmount) as count, sum(totalDebitedEquivalentAmount) as total "
			+ "from CorporateUserPendingTaskModel "
			+ "where transactionService.code=?1 "
			+ "and instructionDate>=?2 "
			+ "and instructionDate<?3 "
			+ "and status in (?4) "
			+ "and userGroupId = ?5 ")
	Object getCountAndTotalByServiceCodeAndInstructionDate(String serviceCode, Date instructionDateFrom, Date instructionDateTo, List<String> pendingTaskStatus, String userGroupId);

	@Query("from CorporateUserPendingTaskModel p where p.senderRefNo = ?1 and p.menu.code = ?2 and p.corporate.id = ?3 and isFinalPayment = 'Y' ")
	List<CorporateUserPendingTaskModel> findBySenderRefNoAndMenuCodeAndCorporateId(String senderRefNo, String menuCode, String corporateId);

	@Modifying
	@Query("update CorporateUserPendingTaskModel p set p.instructionDate = ?2 where p.id = ?1")
	void updateInstructionDatePendingTask(String pendingTaskId, Timestamp instructionDate);
	
    @Modifying
	@Query("update CorporateUserPendingTaskModel p set p.errorTimeoutFlag = ?2 where p.id = ?1")
	void updateErrorTimeoutFlagPendingTask(String pendingTaskID, String errorTimeoutFlag) throws Exception;
	
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Modifying
	@Query("update CorporateUserPendingTaskModel p set p.hostRefNo = ?2, p.retrievalRefNo =?3  where p.id = ?1")
	void updateHostRefNoAndRetrievalRefNoPendingTask(String pendingTaskID, String hostRefNo, String retrievalRefNo) throws Exception;

    @Query("from CorporateUserPendingTaskModel p where p.refNoSpecialRate = ?1 and p.status = 'PENDING'")
	CorporateUserPendingTaskModel findByRefNoSpecialRateAndStatusIsPending(String refNoSpecialRate);
    
}