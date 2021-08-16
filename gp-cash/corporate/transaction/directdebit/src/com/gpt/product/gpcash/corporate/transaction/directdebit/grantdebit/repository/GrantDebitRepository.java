package com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.model.GrantDebitModel;

@Repository
public interface GrantDebitRepository extends JpaRepository<GrantDebitModel, String>, CashRepository<GrantDebitModel>{

	Page<GrantDebitModel> findByCorporateId(String corporateId, Pageable pageInfo) throws Exception;
	
	@Query("from GrantDebitModel grantDebit "
			+ "where grantDebit.corporate.id = ?1 "
			+ "and grantDebit.accountNo = ?2")
	Page<GrantDebitModel> findByCorporateIdAndAccountNo(String corporateId, String accountNo, Pageable pageInfo) throws Exception;
}
