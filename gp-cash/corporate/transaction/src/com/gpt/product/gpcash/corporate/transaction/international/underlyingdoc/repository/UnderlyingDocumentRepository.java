package com.gpt.product.gpcash.corporate.transaction.international.underlyingdoc.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.international.underlyingdoc.model.UnderlyingDocumentModel;

@Repository
public interface UnderlyingDocumentRepository extends JpaRepository<UnderlyingDocumentModel, String>, CashRepository<UnderlyingDocumentModel>{

	Page<UnderlyingDocumentModel> findByCorporateIdAndDeleteFlagOrderByUnderlyingAmountAsc(String corporateId, String isDelete, Pageable pageInfo) throws Exception;
	
	List<UnderlyingDocumentModel> findByDocumentTypeCodeAndUnderlyingAmount(String documentType, BigDecimal underlyingAmount) throws Exception;
	
}