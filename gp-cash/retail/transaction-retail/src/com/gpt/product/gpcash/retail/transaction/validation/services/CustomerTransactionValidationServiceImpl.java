package com.gpt.product.gpcash.retail.transaction.validation.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.calendar.services.CalendarService;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.banktransactionlimit.model.BankTransactionLimitModel;
import com.gpt.product.gpcash.cot.currency.services.CurrencyCOTService;
import com.gpt.product.gpcash.cot.system.services.SystemCOTService;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customeraccount.repository.CustomerAccountRepository;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;
import com.gpt.product.gpcash.retail.customercharge.services.CustomerChargeService;
import com.gpt.product.gpcash.retail.customerlimit.model.CustomerLimitModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.repository.CustomerUserPendingTaskRepository;
import com.gpt.product.gpcash.retail.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTransactionValidationServiceImpl implements CustomerTransactionValidationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private CustomerChargeService customerChargeService;

	@Autowired
	private CustomerUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private SystemCOTService systemCOTService;
	
	@Autowired
	private CurrencyCOTService currencyCOTService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CalendarService calendarService;
	
	@Autowired
	private CustomerAccountRepository customerAccountRepo;
	
	@Override
	public void validateLimit(String customerId, String transactionServiceCode, String accountDtlId,
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate, String instructionMode, Object recurringStartDate)
			throws ApplicationException, BusinessException {
		try {
			customerUtilsRepo.isCustomerValid(customerId);
			
			CustomerAccountModel customerAccount = customerAccountRepo.findByCustomerIdAndAccountId(customerId, accountDtlId);
			
			String sourceAccountCurrency = customerAccount.getAccount().getCurrency()
					.getCode();
			String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);

			Map<String, Object> trxInfoMap = null;
			
			//if recurring maka instructionDate di ambil dari recurringStartDate
			if(!(instructionMode).equals(ApplicationConstants.SI_RECURRING)) {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) instructionDate, instructionMode);
			} else {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) recurringStartDate, instructionMode);
			}
			
			int totalPendingTaskTrxOccurence = ((Number) trxInfoMap.get("totalPendingTaskTrxOccurence")).intValue();
			BigDecimal totalPendingTaskDebitedAmount = (BigDecimal) trxInfoMap.get("totalPendingTaskDebitedAmount");
			
			// validate bank transaction limit
			validateBankTransactionLimit(transactionServiceCode, currencyMatrixCode, transactionCurrency,
					transactionAmount, applicationCode);

			// validate customer limit
			validateCustomerLimit(customerId, transactionServiceCode, currencyMatrixCode, transactionCurrency, transactionAmount,
					applicationCode, totalPendingTaskTrxOccurence, totalPendingTaskDebitedAmount);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void validateBankTransactionLimit(String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode) throws Exception {
		
		BankTransactionLimitModel bankTransactionLimit = productRepo
				.isBankTransactionLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency);

		// TODO implement get equivalent amount for cross forex transaction
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionCurrency : " + transactionCurrency);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("bankTransactionLimit.getMinAmountLimit() : " + bankTransactionLimit.getMinAmountLimit());
			logger.debug("bankTransactionLimit.getMaxAmountLimit() : " + bankTransactionLimit.getMaxAmountLimit());
			logger.debug("\n\n\n\n\n");
		}

		if (bankTransactionLimit != null) {
			if (transactionAmount.compareTo(bankTransactionLimit.getMinAmountLimit()) < 0
					|| transactionAmount.compareTo(bankTransactionLimit.getMaxAmountLimit()) > 0) {
				throw new BusinessException("GPT-0100093");
			}
		}
	}

	private void validateCustomerLimit(String customerId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode, int totalPendingTaskTrxOccurence, BigDecimal totalPendingTaskDebitedAmount) throws Exception {

		CustomerLimitModel customerLimit = customerUtilsRepo
				.isCustomerLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, customerId);

		// TODO implement get equivalent amount for cross forex transaction

		int totalOccurence = customerLimit.getOccurrenceLimitUsage() + totalPendingTaskTrxOccurence;
		BigDecimal totalDebitedAmount = customerLimit.getAmountLimitUsage().add(totalPendingTaskDebitedAmount);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("customerId : " + customerId);
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionCurrency : " + transactionCurrency);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("totalPendingTaskTrxOccurence : " + totalPendingTaskTrxOccurence);
			logger.debug("totalPendingTaskDebitedAmount : " + totalPendingTaskDebitedAmount);
			logger.debug("totalOccurence : " + totalOccurence);
			logger.debug("customerLimit.getMaxOccurrenceLimit() : " + customerLimit.getMaxOccurrenceLimit());
			logger.debug("customerLimit.getAmountLimitUsage() : " + customerLimit.getAmountLimitUsage());
			logger.debug("totalDebitedAmount : " + totalDebitedAmount);
			logger.debug("customerLimit.getMaxAmountLimit( : " + customerLimit.getMaxAmountLimit());
			logger.debug("\n\n\n\n\n");
		}

		if (totalOccurence + 1 > customerLimit.getMaxOccurrenceLimit()) {
			throw new BusinessException("GPT-0100095");
		}

		if (totalDebitedAmount.add(transactionAmount).compareTo(customerLimit.getMaxAmountLimit()) > 0) {
			throw new BusinessException("GPT-0100096");
		}
	}

	public Map<String, Object> getTrxInfoFromPendingTask(String serviceCode, Timestamp instructionDate, String instructionMode)
			throws Exception {
		Map<String, Object> trxInfoMap = new HashMap<>();

		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(instructionDate);
		calFrom.set(Calendar.HOUR_OF_DAY, 0);
		calFrom.set(Calendar.MINUTE, 0);
		calFrom.set(Calendar.SECOND, 0);
		calFrom.set(Calendar.MILLISECOND, 0);

		Calendar calTo = Calendar.getInstance();
		calTo.setTime(instructionDate);
		calTo.set(Calendar.HOUR_OF_DAY, 0);
		calTo.set(Calendar.MINUTE, 0);
		calTo.set(Calendar.SECOND, 0);
		calTo.set(Calendar.MILLISECOND, 0);
		calTo.add(Calendar.DAY_OF_MONTH, 1);
		
		//search pending task which the transaction not being released yet
		List<String> pendingTaskStatus = new ArrayList<>();
		pendingTaskStatus.add(ApplicationConstants.WF_STATUS_PENDING);
		
		if(ApplicationConstants.SI_FUTURE_DATE.equals(instructionMode) || ApplicationConstants.SI_RECURRING.equals(instructionMode)) {
			pendingTaskStatus.add(ApplicationConstants.WF_STATUS_APPROVED);
		}
		
		Object[] totals = (Object[])pendingTaskRepo.getCountAndTotalByServiceCodeAndInstructionDate(serviceCode, calFrom.getTime(), calTo.getTime(), 
				pendingTaskStatus);

		trxInfoMap.put("totalPendingTaskTrxOccurence", totals[0]);
		trxInfoMap.put("totalPendingTaskDebitedAmount", totals[1] == null ? new BigDecimal(0) : totals[1]);
		return trxInfoMap;
	}

	private String getCurrencyMatrixCode(String fromCurrency, String toCurrency)
			throws Exception {
		String localCurrency = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
		
		if (fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_LL;
		} else if ((fromCurrency.equalsIgnoreCase(localCurrency) || toCurrency.equalsIgnoreCase(localCurrency))
				&& !toCurrency.equalsIgnoreCase(fromCurrency)) {
			return ApplicationConstants.CCY_MTRX_LF;
		} else if (!fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_FS;
		} else if (!fromCurrency.equalsIgnoreCase(localCurrency) && !toCurrency.equalsIgnoreCase(localCurrency)
				&& !fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_FC;
		}

		return null;
	}
	
	@Override
	public void validateInstructionMode(String instructionMode, Timestamp instructionDate, String recurringParamType, int recurringParam,
			Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime) throws ApplicationException, BusinessException {

		//if mode immediate or future then instructionDate mandatory
		if(ApplicationConstants.SI_IMMEDIATE.equals(instructionMode)){
			if(!ValueUtils.hasValue(instructionDate)){
				throw new BusinessException("GPT-0100101");
			}
			
			if(Helper.formatTimestampToString(instructionDate, ApplicationConstants.DATE_FORMAT).compareTo(
					Helper.formatTimestampToString(DateUtils.getCurrentTimestamp(), ApplicationConstants.DATE_FORMAT)) != 0){
				throw new BusinessException("GPT-0100106");
			}
			
			//if immediate then sessionTime must 00:00
			if(!"00:00".equals(sessionTime)) {
				throw new BusinessException("GPT-0100148");
			}
		} else if(ApplicationConstants.SI_FUTURE_DATE.equals(instructionMode)){
			if(!ValueUtils.hasValue(instructionDate)){
				throw new BusinessException("GPT-0100101");
			}
			
			//checking for session time
			if(!ValueUtils.hasValue(sessionTime)){
				throw new BusinessException("GPT-0100148");
			} else {
				List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
				boolean foundSession = false;
				for(String sessionTimeRegistered : sessionTimeList) {
					if(sessionTimeRegistered.equals(sessionTime)) {
						foundSession = true;
					}
				}
				
				if(!foundSession) {
					throw new BusinessException("GPT-0100148");
				}
			}
			//end checking for session time
			
			
			Calendar tommorowDate = DateUtils.getNextEarliestDate(DateUtils.getCurrentDate());
			//future date must bigger than today date
			if(instructionDate.compareTo(tommorowDate.getTime()) < 0){
				throw new BusinessException("GPT-0100142");
			}
		} else if(ApplicationConstants.SI_RECURRING.equals(instructionMode)){
			//checking for session time
			if(!ValueUtils.hasValue(sessionTime)){
				throw new BusinessException("GPT-0100148");
			} else {
				List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
				boolean foundSession = false;
				for(String sessionTimeRegistered : sessionTimeList) {
					if(sessionTimeRegistered.equals(sessionTime)) {
						foundSession = true;
					}
				}
				
				if(!foundSession) {
					throw new BusinessException("GPT-0100148");
				}
			}
			//end checking for session time
			
			if(!ValueUtils.hasValue(recurringParamType)){
				throw new BusinessException("GPT-0100102");
			}
			
			if(recurringParam == 0){
				throw new BusinessException("GPT-0100103");
			}
			
			if(!ValueUtils.hasValue(recurringStartDate)){
				throw new BusinessException("GPT-0100104");
			}
			
			if(!ValueUtils.hasValue(recurringEndDate)){
				throw new BusinessException("GPT-0100105");
			}
			
			if(recurringStartDate.compareTo(recurringEndDate) > 0){
				throw new BusinessException("GPT-0100109");
			}
		}
	}
	
	@Override
	public void validateChargeAndTotalTransaction(String customerId, String transactionServiceCode, BigDecimal transactionAmount,
			BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode, List<Map<String, Object>> chargeList) throws ApplicationException, BusinessException {
		try{
			//validate charging & totalDebitedAmount
			Map<String, Object> chargesInfo =  customerChargeService.getCustomerCharges(applicationCode,
					transactionServiceCode, customerId);
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get("totalCharge"));
			BigDecimal totalTransaction = transactionAmount.add(totalChargeFromTable);
			
			logger.debug("\n\n\n\n\n");
			logger.debug("totalChargeFromTable = " + totalChargeFromTable);
			logger.debug("totalTransaction = " + totalTransaction);
			logger.debug("\n\n\n\n\n");
			
			if(totalCharge.compareTo(totalChargeFromTable) != 0 || totalTransaction.compareTo(totalDebitedAmount) != 0){
				throw new BusinessException("GPT-0100107");
			}
			
			//check if charge id and value valid
			for(Map<String, Object> chargeMap : chargeList){
				CustomerChargeModel customerCharge = customerUtilsRepo.isCustomerChargeValid((String) chargeMap.get("id"));
				
				logger.debug("\n\n\n\n\n");
				logger.debug("customerCharge.getValue() = " + customerCharge.getValue());
				logger.debug("chargeMap.get(value) = " + (BigDecimal) chargeMap.get("value"));
				logger.debug("\n\n\n\n\n");
				
				if(customerCharge.getValue().compareTo((BigDecimal) chargeMap.get("value")) != 0){
					throw new BusinessException("GPT-0100107");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void validateChargeAndTotalTransactionPerRecords(String customerId, String transactionServiceCode, BigDecimal transactionAmount,
			BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode, List<Map<String, Object>> chargeList, Long records) throws ApplicationException, BusinessException {
		try{
			//validate charging & totalDebitedAmount
			Map<String, Object> chargesInfo =  customerChargeService.getCustomerChargesPerRecord(applicationCode,
					transactionServiceCode, customerId, records);
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get("totalCharge"));
			BigDecimal totalTransaction = transactionAmount.add(totalChargeFromTable);
			
			logger.debug("\n\n\n\n\n");
			logger.debug("totalChargeFromTable = " + totalChargeFromTable);
			logger.debug("totalTransaction = " + totalTransaction);
			logger.debug("totalRecords = " + records);
			logger.debug("\n\n\n\n\n");
			
			if(totalCharge.compareTo(totalChargeFromTable) != 0 || totalTransaction.compareTo(totalDebitedAmount) != 0){
				throw new BusinessException("GPT-0100107");
			}
			
			//check if charge id and value valid
			for(Map<String, Object> chargeMap : chargeList){
				CustomerChargeModel customerCharge = customerUtilsRepo.isCustomerChargeValid((String) chargeMap.get("id"));
				
				logger.debug("\n\n\n\n\n");
				logger.debug("customerCharge.getValue() = " + customerCharge.getValue());
				logger.debug("chargeMap.get(value) = " + (BigDecimal) chargeMap.get("value"));
				logger.debug("\n\n\n\n\n");
				
				if(customerCharge.getValue().compareTo((BigDecimal) chargeMap.get("value")) != 0){
					throw new BusinessException("GPT-0100107");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void validateWorkingDay(String customerId, Date transactionDate)
			throws ApplicationException, BusinessException {
		
	}
	
	@Override
	public Timestamp getInstructionDateForRelease(String instructionMode, Timestamp instructionDate) throws Exception{
		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			instructionDate = DateUtils.getCurrentTimestamp();
		}
		
		return instructionDate;
	}
	
	@Override
	public void validateCOT(String instructionMode, String serviceCode, String currencyCode,
			String applicationCode, String sessionTime, boolean isReleaseTrx, boolean isCheckCurrencyCOT)  throws ApplicationException, BusinessException {
		
		try{
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("instructionMode : " + instructionMode);
				logger.debug("serviceCode : " + serviceCode);
				logger.debug("currencyCode : " + currencyCode);
				logger.debug("applicationCode : " + applicationCode);
				logger.debug("sessionTime : " + sessionTime);
				logger.debug("isReleaseTrx : " + isReleaseTrx);
				logger.debug("\n\n\n\n\n");
			}
			
			if(ApplicationConstants.SI_IMMEDIATE.equals(instructionMode)){
					systemCOTService.validateSystemCOT(serviceCode, applicationCode);
				
				if(isCheckCurrencyCOT)
					currencyCOTService.validateSystemCOT(currencyCode, applicationCode);
			} else if(ApplicationConstants.SI_FUTURE_DATE.equals(instructionMode)){
				systemCOTService.validateSystemCOTWithSessionTime(serviceCode, applicationCode, sessionTime);
				
				if(isCheckCurrencyCOT)
					currencyCOTService.validateSystemCOT(currencyCode, applicationCode, sessionTime);
				
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void validateHoliday(String instructionMode, Timestamp instructionDate, String recurringParamType, int recurringParam,
			Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime)  throws ApplicationException, BusinessException {
		
		if(ApplicationConstants.SI_RECURRING.equals(instructionMode)) {
			instructionDate = InstructionModeUtils.getStandingInstructionDate(recurringParamType, 
					recurringParam, 
					recurringStartDate);
		}
		
		if(calendarService.isHoliday(instructionDate)) {
			throw new BusinessException("GPT-0100200");
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void updateTransactionLimit(String customerId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, BigDecimal transactionAmount, String applicationCode) throws Exception {
		
		String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);
		
		updateCustomerLimit(customerId, serviceCode, currencyMatrixCode, transactionCurrency, transactionAmount, applicationCode);
	}
	
	private void updateCustomerLimit(String customerId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode) throws Exception {

		CustomerLimitModel customerLimit = customerUtilsRepo
				.isCustomerLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, customerId);
		
		if (customerLimit.getOccurrenceLimitUsage() + 1 > customerLimit.getMaxOccurrenceLimit())
			throw new BusinessException("GPT-0100095");

		if (customerLimit.getAmountLimitUsage().add(transactionAmount).compareTo(customerLimit.getMaxAmountLimit()) > 0)
			throw new BusinessException("GPT-0100096");

		customerUtilsRepo.getCustomerLimitRepo().updateCustomerLimit(transactionAmount, customerLimit.getId());
	}	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void reverseUpdateTransactionLimit(String customerId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, BigDecimal transactionAmount, String applicationCode) throws Exception {
		
		String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);
		
		reverseUpdateCustomerLimit(customerId, serviceCode, currencyMatrixCode, transactionCurrency, transactionAmount, applicationCode);
	}
	
	private void reverseUpdateCustomerLimit(String customerId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode) throws Exception {

		CustomerLimitModel customerLimit = customerUtilsRepo
				.isCustomerLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, customerId);
		
		customerUtilsRepo.getCustomerLimitRepo().reverseUpdateCustomerLimit(transactionAmount, customerLimit.getId());
	}
}
