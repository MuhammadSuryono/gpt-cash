package com.gpt.product.gpcash.retail.transaction.validation.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerTransactionValidationService {
	void validateWorkingDay(String customerId, Date transactionDate)
			throws ApplicationException, BusinessException;
	
	Timestamp getInstructionDateForRelease(String instructionMode, Timestamp instructionDate) throws Exception;

	void validateChargeAndTotalTransaction(String customerId, String transactionServiceCode,
			BigDecimal transactionAmount, BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode,
			List<Map<String, Object>> chargeList) throws ApplicationException, BusinessException;

	void updateTransactionLimit(String customerId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, BigDecimal transactionAmount,
			String applicationCode) throws Exception;

	void reverseUpdateTransactionLimit(String customerId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, BigDecimal transactionAmount,
			String applicationCode) throws Exception;

	void validateInstructionMode(String instructionMode, Timestamp instructionDate, String recurringParamType,
			int recurringParam, Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime)
			throws ApplicationException, BusinessException;

	void validateLimit(String customerId, String transactionServiceCode, String accountDtlId,
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate,
			String instructionMode, Object recurringStartDate) throws ApplicationException, BusinessException;

	void validateHoliday(String instructionMode, Timestamp instructionDate, String recurringParamType,
			int recurringParam, Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime)
			throws ApplicationException, BusinessException;

	void validateCOT(String instructionMode, String serviceCode, String currencyCode, String applicationCode,
			String sessionTime, boolean isReleaseTrx, boolean isCheckCurrencyCOT)
			throws ApplicationException, BusinessException;
	
	void validateChargeAndTotalTransactionPerRecords(String customerId, String transactionServiceCode,
			BigDecimal transactionAmount, BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode,
			List<Map<String, Object>> chargeList, Long records) throws ApplicationException, BusinessException;
	
}
