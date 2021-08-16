package com.gpt.product.gpcash.retail.token.tokenuser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;

@Repository
public interface CustomerTokenUserRepository extends JpaRepository<CustomerTokenUserModel, String>, CashRepository<CustomerTokenUserModel> {
	
	@Query("from CustomerTokenUserModel tokenUser "
			+ "where tokenUser.tokenNo = ?1 "
			+ "and tokenUser.assignedUser.code = ?2")
	CustomerTokenUserModel findByTokenNoAndAssignedUser(String tokenNo, String assignedUser) throws Exception;
	
	CustomerTokenUserModel findByAssignedUserCode(String customerId) throws Exception;
	
	CustomerTokenUserModel findByTokenNo(String tokenNo) throws Exception;
}

