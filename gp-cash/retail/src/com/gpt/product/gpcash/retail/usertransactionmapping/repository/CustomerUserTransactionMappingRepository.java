package com.gpt.product.gpcash.retail.usertransactionmapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.usertransactionmapping.model.CustomerUserTransactionMappingModel;

@Repository
public interface CustomerUserTransactionMappingRepository extends JpaRepository<CustomerUserTransactionMappingModel, String>, CashRepository<CustomerUserTransactionMappingModel>{

	@Modifying
	@Query("update CustomerUserTransactionMappingModel set totalExecutedTransaction = 0 ") 
	void resetTotalExecuted();
}
