package com.gpt.product.gpcash.maintenance.timedeposit.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.maintenance.timedeposit.product.model.TimeDepositTermModel;

@Repository
public interface TimeDepositTermRepository extends JpaRepository<TimeDepositTermModel, String>, CashRepository<TimeDepositTermModel> {
	
	Page<TimeDepositTermModel> findByProductType_CodeAndDeleteFlagOrderByTermParamAsc(String productCode, String isDelete, Pageable pageInfo) throws Exception;
	
	TimeDepositTermModel findByCodeAndProductType_code(String code, String productCode) throws Exception;
	
	@Query("FROM TimeDepositTermModel term "
			+ "WHERE term.productType.code= ?1 "
			+ "ORDER BY term.termParam ASC")
	List<TimeDepositTermModel> searchTermByProductCode(String productCode) throws Exception;
	
	@Modifying
	@Query("DELETE FROM TimeDepositTermModel term WHERE term.productType.code= ?1")
	void deleteTermByProduct(String productCode);
}
