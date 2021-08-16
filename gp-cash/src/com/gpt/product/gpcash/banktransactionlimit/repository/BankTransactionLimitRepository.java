package com.gpt.product.gpcash.banktransactionlimit.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.banktransactionlimit.model.BankTransactionLimitModel;

@Repository
public interface BankTransactionLimitRepository extends JpaRepository<BankTransactionLimitModel, String>, CashRepository<BankTransactionLimitModel> {
	@Query("select a.id, a.service.code, b.name, a.minAmountLimit, a.maxAmountLimit, a.currency.code, c.name, e.code, e.name "
			 + "from BankTransactionLimitModel a "
		     + "left join a.service b "
		     + "left join a.currency c "
		     + "left join a.serviceCurrencyMatrix d "
			 + "left join d.currencyMatrix e "
			 + "where a.application.code = (?1) "
			 + "and b.serviceType.code = (?2) "
			 + "and b.deleteFlag = 'N' "
			 + "order by b.idx")
	Page<Object[]> findDetailByServiceTypeCode(String applicationCode, String serviceTypeCode, Pageable pageInfo) throws Exception;
		
	@Query("select a.id, a.service.code, b.name, a.minAmountLimit, a.maxAmountLimit, a.currency.code, c.name, e.code, e.name "
			+ "from BankTransactionLimitModel a "
			+ "left join a.service b "
			+ "left join a.currency c "
			+ "left join a.serviceCurrencyMatrix d "
			+ "left join d.currencyMatrix e "
			+ "where a.application.code = (?1) "
			+ "and a.id in (?2) "
			+ "and b.deleteFlag = 'N' "
			+ "order by b.idx")
	Page<Object[]> findDetailByIds(String applicationCode, List<String> bankTransactionLimitId, Pageable pageInfo) throws Exception;
		
	@Query("select a from BankTransactionLimitModel a "
				 + "where a.application.code = ?1 "
				 + "and a.service.code = ?2 "
				 + "and a.serviceCurrencyMatrix.currencyMatrix.code = ?3 "
				 + "and a.currency.code = ?4 "
				 + "and a.service.deleteFlag = 'N'")
	BankTransactionLimitModel findByServiceCodeAndCurrencyMatrixCode(String applicationCode, String serviceCode, String currencyMatrixCode, String currencyCode) throws Exception;
}
