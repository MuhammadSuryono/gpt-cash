package com.gpt.product.gpcash.corporate.usertransactionmapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.usertransactionmapping.model.UserTransactionMappingModel;

@Repository
public interface UserTransactionMappingRepository extends JpaRepository<UserTransactionMappingModel, String>, CashRepository<UserTransactionMappingModel>{

	@Modifying
	@Query("update UserTransactionMappingModel set totalExecutedTransaction = 0 ") 
	void resetTotalExecuted();
}
