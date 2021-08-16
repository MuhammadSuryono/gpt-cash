package com.gpt.product.gpcash.corporate.token.tokenuser.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;

@Repository
public interface TokenUserRepository extends JpaRepository<TokenUserModel, String>, CashRepository<TokenUserModel> {
	
	@Query("from TokenUserModel tokenUser "
			+ "where tokenUser.corporate.id= ?1 "
			+ "and tokenUser.tokenType.code = ?2 "
			+ "and tokenUser.status = 'Active'")
	List<TokenUserModel> findByCorporateIdAndTokenTypeCodeOrderByTokenNo(String corporateId, String tokenType) throws Exception;
	
	@Query("from TokenUserModel tokenUser "
			+ "where tokenUser.corporate.id= ?1 "
			+ "and tokenUser.tokenType.code = ?2 "
			+ "and tokenUser.status = 'Active' "
			+ "and tokenUser.assignedUser.code is null")
	List<TokenUserModel> findUnassignTokenByCorporateIdAndTokenTypeCodeOrderByTokenNo(String corporateId, String tokenType) throws Exception;
	
	TokenUserModel findByCorporateIdAndTokenNo(String corporateId, String tokenNo) throws Exception;
	
	@Query("from TokenUserModel tokenUser "
			+ "where tokenUser.corporate.id= ?1 "
			+ "and tokenUser.tokenNo = ?2 "
			+ "and tokenUser.assignedUser.code = ?3")
	TokenUserModel findByCorporateIdAndTokenNoAndAssignedUser(String corporateId, String tokenNo, String assignedUser) throws Exception;
	
	@Query("from TokenUserModel tokenUser "
			+ "where tokenUser.corporate.id= ?1 "
			+ "and tokenUser.tokenNo = ?2 "
			+ "and tokenUser.assignedUser.code =?3 "
			+ "and tokenUser.status = 'Active'")
	TokenUserModel searchByTokenNoActive(String corporateId, String tokenNo, String userCode) throws Exception;
	
	TokenUserModel findByAssignedUserCode(String userCode) throws Exception;
	
	TokenUserModel findByTokenNo(String tokenNo) throws Exception;
}
