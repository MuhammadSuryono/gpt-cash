package com.gpt.product.gpcash.corporate.authorizedlimitscheme.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;

@Repository
public interface AuthorizedLimitSchemeRepository extends JpaRepository<AuthorizedLimitSchemeModel, String>, CashRepository<AuthorizedLimitSchemeModel>{

	AuthorizedLimitSchemeModel findByCorporateIdAndApprovalLevelCode(String corporateId, String approvalLevelCode) throws Exception;
	
	Page<AuthorizedLimitSchemeModel> findByCorporateId(String corporateId, Pageable pageInfo) throws Exception;
	
	List<AuthorizedLimitSchemeModel> findByCorporateIdOrderByApprovalLevelCode(String corporateId) throws Exception;
	
	List<AuthorizedLimitSchemeModel> findByCorporateIdAndId(String corporateId, String authorizedLimitId) throws Exception;
}