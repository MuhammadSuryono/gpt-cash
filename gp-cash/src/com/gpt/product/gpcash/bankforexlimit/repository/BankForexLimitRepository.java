package com.gpt.product.gpcash.bankforexlimit.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.bankforexlimit.model.BankForexLimitModel;

@Repository
public interface BankForexLimitRepository extends JpaRepository<BankForexLimitModel, String>, CashRepository<BankForexLimitModel> {
	
	@Modifying
	@Query("update BankForexLimitModel set buyLimitUsage = 0, sellLimitUsage = 0")
	void resetLimit();
	
	@Modifying
	@Query("update BankForexLimitModel set limitUsage = limitUsage + ?2, limitUsageEquivalent = limitUsageEquivalent + ?3  where currency.code = ?1")
	void updateBankForexBuyLimitUsage(String currencyCode,BigDecimal trxAmount,BigDecimal eqAmount);
	
	@Modifying
	@Query("update BankForexLimitModel set limitUsage = limitUsage - ?2, limitUsageEquivalent = limitUsageEquivalent - ?3  where currency.code = ?1")
	void updateBankForexSellLimitUsage(String currencyCode,BigDecimal trxAmount,BigDecimal eqAmount);
	
}
