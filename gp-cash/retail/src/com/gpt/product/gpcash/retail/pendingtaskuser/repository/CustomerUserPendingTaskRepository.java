package com.gpt.product.gpcash.retail.pendingtaskuser.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

@Repository
public interface CustomerUserPendingTaskRepository extends JpaRepository<CustomerUserPendingTaskModel, String>, CustomerUserPendingTaskCustomRepository {
	
	@Modifying(clearAutomatically = true)
	@Query("update CustomerUserPendingTaskModel p set p.activityDate = ?2, p.activityBy = ?3, p.trxStatus = ?4 where p.id = ?1")
	void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy, CustomerTransactionStatus trxStatus);

	@Modifying
	@Query("update CustomerUserPendingTaskModel p set p.status = ?2, p.activityDate = ?3, p.activityBy = ?4, p.trxStatus = ?5 where p.id = ?1")
	void updatePendingTask(String pendingTaskId, String status, Timestamp activityDate, String activityBy, CustomerTransactionStatus trxStatus);
	
	@Query("from CustomerUserPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.referenceNo = ?1")
	CustomerUserPendingTaskModel findByReferenceNo(String referenceNo);

	@Query("from CustomerUserPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.id = ?1")
	CustomerUserPendingTaskModel findById(String id);
	
	@Query("from CustomerUserPendingTaskModel p where p.referenceNo = ?1 and p.status = 'PENDING'")
	CustomerUserPendingTaskModel findByReferenceNoAndStatusIsPending(String referenceNo);
	
	List<CustomerUserPendingTaskModel> findByUniqueKeyAndStatusAndMenuCodeAndCustomerId(String uniqueKey, String status, String menuCode, String customerId);
	
	List<CustomerUserPendingTaskModel> findByUniqueKeyContainingAndStatusAndMenuCodeAndCustomerId(String uniqueKey, String status, String menuCode, String customerId);

	@Modifying
	@Query("update CustomerUserPendingTaskModel p set p.status = ?2 where p.id = ?1")
	void updateStatusPendingTask(String pendingTaskID, String status) throws Exception;

	@Query("from CustomerUserPendingTaskModel where instructionDate between ?1 and ?2 and instructionMode in (?3) and menu.menuType.code = 'T' and status = 'PENDING'")
	List<CustomerUserPendingTaskModel> findExpiredPendingTask(Timestamp startExpiryDate, Timestamp endExpiryDate, List<String> instructionMode);

	@Query("select count(totalDebitedEquivalentAmount) as count, sum(totalDebitedEquivalentAmount) as total "
			+ "from CustomerUserPendingTaskModel "
			+ "where transactionService.code=?1 "
			+ "and instructionDate>=?2 "
			+ "and instructionDate<?3 "
			+ "and status in (?4) ")
	Object getCountAndTotalByServiceCodeAndInstructionDate(String serviceCode, Date instructionDateFrom, Date instructionDateTo, List<String> pendingTaskStatus);

	@Query("from CustomerUserPendingTaskModel p where p.senderRefNo = ?1 and p.menu.code = ?2 and p.customer.id = ?3 and isFinalPayment = 'Y' ")
	List<CustomerUserPendingTaskModel> findBySenderRefNoAndMenuCodeAndCustomerId(String senderRefNo, String menuCode, String customerId);
}