package com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.model.CustomerUnderlyingDocumentModel;

@Repository
public interface CustomerUnderlyingDocumentRepository extends JpaRepository<CustomerUnderlyingDocumentModel, String>, CashRepository<CustomerUnderlyingDocumentModel>{

	Page<CustomerUnderlyingDocumentModel> findByCustomerIdAndDeleteFlagOrderByUnderlyingAmountAsc(String customerId, String isDelete, Pageable pageInfo) throws Exception;
	
	List<CustomerUnderlyingDocumentModel> findByDocumentTypeCodeAndUnderlyingAmount(String documentType, BigDecimal underlyingAmount) throws Exception;
	
}