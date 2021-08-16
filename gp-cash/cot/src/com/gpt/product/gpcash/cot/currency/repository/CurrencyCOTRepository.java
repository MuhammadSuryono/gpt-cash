package com.gpt.product.gpcash.cot.currency.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.cot.currency.model.CurrencyCOTModel;

@Repository
public interface CurrencyCOTRepository extends JpaRepository<CurrencyCOTModel, String>, CashRepository<CurrencyCOTModel>{
	
	@Cacheable(cacheNames = "CurrencyCOT", key = "#p0 + #p1")
	CurrencyCOTModel findByCodeAndApplicationCode(String code, String applicationCode) throws Exception;

}
