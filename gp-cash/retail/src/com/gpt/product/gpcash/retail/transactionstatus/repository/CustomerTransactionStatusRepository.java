package com.gpt.product.gpcash.retail.transactionstatus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;

@Repository
public interface CustomerTransactionStatusRepository extends JpaRepository<CustomerTransactionStatusModel, String>, CashRepository<CustomerTransactionStatusModel>  {
	
	@Query("from CustomerTransactionStatusModel transactionStatus "
			+ "where transactionStatus.pendingTaskId = ?1 "
			+ "order by activityDate DESC")
	List<CustomerTransactionStatusModel> findByPendingTaskId(String pendingTaskId) throws Exception;
}
