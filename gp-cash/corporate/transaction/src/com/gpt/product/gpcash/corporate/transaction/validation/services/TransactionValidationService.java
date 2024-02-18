package com.gpt.product.gpcash.corporate.transaction.validation.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface TransactionValidationService {
	void validateWorkingDay(String corporateId, Date transactionDate)
			throws ApplicationException, BusinessException;
	
	Timestamp getInstructionDateForRelease(String instructionMode, Timestamp instructionDate) throws Exception;

	void validateChargeAndTotalTransaction(String corporateId, String transactionServiceCode,
			BigDecimal transactionAmount, BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode,
			List<Map<String, Object>> chargeList, String chargeInstruction) throws ApplicationException, BusinessException;

	Timestamp validateExpiryDate(String instructionMode, Timestamp instructionDate) throws Exception;

	void updateTransactionLimit(String corporateId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, String corporateUserGroupId, BigDecimal transactionAmount,
			String applicationCode) throws Exception;
	
	void updateTransactionLimitEquivalent(String corporateId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, String corporateUserGroupId, BigDecimal transactionAmount,
			String applicationCode, BigDecimal totalCharge) throws Exception;
		

	void reverseUpdateTransactionLimit(String corporateId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, String corporateUserGroupId, BigDecimal transactionAmount,
			String applicationCode) throws Exception;

	void validateInstructionMode(String instructionMode, Timestamp instructionDate, String recurringParamType,
			int recurringParam, Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime)
			throws ApplicationException, BusinessException;

	void validateLimit(String userCode, String corporateId, String transactionServiceCode, String accountGroupDtlId,
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate,
			String instructionMode, Object recurringStartDate) throws ApplicationException, BusinessException;

	void validateHoliday(String instructionMode, Timestamp instructionDate, String recurringParamType,
			int recurringParam, Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime,boolean isRelease, String transactionCurrency)
			throws ApplicationException, BusinessException;

	void validateCOT(String instructionMode, String serviceCode, String currencyCode, String applicationCode,
			String sessionTime, boolean isReleaseTrx, boolean isCheckCurrencyCOT)
			throws ApplicationException, BusinessException;
	
	void validateMaximumRecordBulkTransaction(int totalRecord) throws ApplicationException, BusinessException;

	void validateChargeAndTotalTransactionPerRecords(String corporateId, String transactionServiceCode,
			BigDecimal transactionAmount, BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode,
			List<Map<String, Object>> chargeList, Long records) throws ApplicationException, BusinessException;
	
	void validateMinimumPayment(BigDecimal transactionAmount,BigDecimal minimumPayment) throws ApplicationException, BusinessException;
	
	Map<String, Object> validateLimitEquivalent(String userCode, String corporateId, String transactionServiceCode, String accountGroupDtlId,
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate,
			String instructionMode, Object recurringStartDate, String exchangeRate,String treasuryCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> validateCounterRate(String sourceAccountCurrency, String transactionCurrency, BigDecimal transactionAmount) throws ApplicationException, BusinessException;
	Map<String, Object> checkSpecialRate(String treasuryCode, String sourceAccountCurrency, 
			String transactionCurrency, BigDecimal transactionAmount, Timestamp instructionDate, 
			String corporateId, String instructionMode) throws ApplicationException, BusinessException;

	void updateBankForexLimit(String code, String transactionCurrency, BigDecimal transactionAmount,
			BigDecimal totalDebitedEquivalentAmount) throws Exception;
	
	Map<String, Object> checkForwardContract(String treasuryCode, String sourceAccountCurrency, String transactionCurrency, BigDecimal transactionAmount, Timestamp instructionDate, 
			String corporateId, String instructionMode) throws ApplicationException, BusinessException;
	
}
