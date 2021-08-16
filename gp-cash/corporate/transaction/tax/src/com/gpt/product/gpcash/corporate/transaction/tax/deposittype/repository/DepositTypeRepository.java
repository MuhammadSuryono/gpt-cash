package com.gpt.product.gpcash.corporate.transaction.tax.deposittype.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.model.DepositTypeModel;

@Repository
public interface DepositTypeRepository extends JpaRepository<DepositTypeModel, String>, CashRepository<DepositTypeModel>{
	
	List<DepositTypeModel> findByTaxDepositCode(String depositCode) throws Exception;
	
	Page<DepositTypeModel> findByTaxType_CodeAndDeleteFlagOrderByTaxDepositCodeAsc(String taxTypeCode, String isDelete, Pageable pageInfo) throws Exception;

}