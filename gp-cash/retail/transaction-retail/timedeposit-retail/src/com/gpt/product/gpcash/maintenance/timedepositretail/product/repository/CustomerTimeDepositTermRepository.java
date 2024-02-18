package com.gpt.product.gpcash.maintenance.timedepositretail.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositTermModel;

@Repository
public interface CustomerTimeDepositTermRepository extends JpaRepository<CustomerTimeDepositTermModel, String>, CashRepository<CustomerTimeDepositTermModel> {
	
	Page<CustomerTimeDepositTermModel> findByProductType_CodeAndDeleteFlagOrderByTermParamAsc(String productCode, String isDelete, Pageable pageInfo) throws Exception;
	
	CustomerTimeDepositTermModel findByCodeAndProductType_code(String code, String productCode) throws Exception;
	
	@Query("FROM CustomerTimeDepositTermModel term "
			+ "WHERE term.productType.code= ?1 "
			+ "ORDER BY term.termParam ASC")
	List<CustomerTimeDepositTermModel> searchTermByProductCode(String productCode) throws Exception;
	
	@Modifying
	@Query("DELETE FROM CustomerTimeDepositTermModel term WHERE term.productType.code= ?1")
	void deleteTermByProduct(String productCode);
}
