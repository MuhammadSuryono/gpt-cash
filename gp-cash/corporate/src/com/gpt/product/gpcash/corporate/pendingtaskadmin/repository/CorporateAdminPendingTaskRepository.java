package com.gpt.product.gpcash.corporate.pendingtaskadmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;

@Repository
public interface CorporateAdminPendingTaskRepository extends JpaRepository<CorporateAdminPendingTaskModel, String>, CorporateAdminPendingTaskCustomRepository {
	
	@Query("from CorporateAdminPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.referenceNo = ?1")
	CorporateAdminPendingTaskModel findByReferenceNo(String referenceNo);

	@Query("from CorporateAdminPendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.id = ?1")
	CorporateAdminPendingTaskModel findById(String id);
	
	@Query("from CorporateAdminPendingTaskModel p where p.referenceNo = ?1 and p.status = 'PENDING'")
	CorporateAdminPendingTaskModel findByReferenceNoAndStatusIsPending(String referenceNo);
	
	List<CorporateAdminPendingTaskModel> findByUniqueKeyAndStatusAndMenuCodeAndCorporateId(String uniqueKey, String status, String menuCode, String corporateId);
	
	List<CorporateAdminPendingTaskModel> findByUniqueKeyContainingAndStatusAndMenuCodeAndCorporateId(String uniqueKey, String status, String menuCode, String corporateId);

	@Modifying
	@Query("update CorporateAdminPendingTaskModel p set p.status = ?2 where p.id = ?1")
	void updateStatusPendingTask(String pendingTaskID, String status) throws Exception;
	
}
