package com.gpt.component.pendingtask.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.pendingtask.model.PendingTaskModel;

@Repository
public interface PendingTaskRepository extends JpaRepository<PendingTaskModel, String>, PendingTaskCustomRepository {
	
	@Query("from PendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.referenceNo = ?1")
	PendingTaskModel findByReferenceNo(String referenceNo);

	@Query("from PendingTaskModel p left join fetch p.values left join fetch p.oldValues where p.id = ?1")
	PendingTaskModel findById(String id);
	
	@Query("from PendingTaskModel p where p.referenceNo = ?1 and p.status = 'PENDING'")
	PendingTaskModel findByReferenceNoAndStatusIsPending(String referenceNo);
	
	List<PendingTaskModel> findByUniqueKeyAndStatusAndMenuCode(String uniqueKey, String status, String menuCode);
	
	List<PendingTaskModel> findByUniqueKeyContainingAndStatusAndMenuCode(String uniqueKey, String status, String menuCode);

	@Modifying
	@Query("update PendingTaskModel p set p.status = ?2 where p.id = ?1")
	void updateStatusPendingTask(String pendingTaskID, String status) throws Exception;

}
