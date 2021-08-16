package com.gpt.product.gpcash.corporate.corporateaccount.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;

@Repository
public interface CorporateAccountRepository extends JpaRepository<CorporateAccountModel, String>, CashRepository<CorporateAccountModel>{

	Page<CorporateAccountModel> findByCorporateId(String corporateId, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateAccountModel corpAccount "
			+ "where corpAccount.corporate.id = ?1 "
			+ "and corpAccount.account.accountNo = ?2")
	Page<CorporateAccountModel> findByCorporateIdAndAccountNo(String corporateId, String accountNo, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateAccountModel corpAccount "
			+ "where corpAccount.corporate.id = ?1 "
			+ "and corpAccount.account.accountNo like ?2")
	Page<CorporateAccountModel> findByCorporateIdAndAccountNoLike(String corporateId, String accountNo, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateAccountModel corpAccount "
			+ "where corpAccount.corporate.id = ?1 "
			+ "and corpAccount.account.accountType.code in (?2)")
	Page<CorporateAccountModel> findCASAAccountByCorporate(String corporateId, List<String> accountTypeList, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateAccountModel corpAccount "
			+ "where corpAccount.corporate.id = ?1 "
			+ "and corpAccount.account.accountType.code = '002'"
			+ "and corpAccount.account.hostCifId in (select c.hostCifId from CorporateModel c where c.id = ?1)")
	List<CorporateAccountModel> findCAAccountForVAByCorporate(String corporateId) throws Exception;
}