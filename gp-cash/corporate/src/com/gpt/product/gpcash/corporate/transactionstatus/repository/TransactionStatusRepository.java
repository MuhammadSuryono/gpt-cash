package com.gpt.product.gpcash.corporate.transactionstatus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;

@Repository
public interface TransactionStatusRepository extends JpaRepository<TransactionStatusModel, String>, CashRepository<TransactionStatusModel>  {
	
	@Query("from TransactionStatusModel transactionStatus "
			+ "where transactionStatus.pendingTaskId = ?1 "
			+ "order by activityDate DESC")
	List<TransactionStatusModel> findByPendingTaskId(String pendingTaskId) throws Exception;
}
