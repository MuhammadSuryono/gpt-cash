package com.gpt.product.gpcash.corporate.transaction.validation.services;

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
import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.component.maintenance.exchangerate.repository.ExchangeRateRepository;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.bankforexlimit.model.BankForexLimitModel;
import com.gpt.product.gpcash.banktransactionlimit.model.BankTransactionLimitModel;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;
import com.gpt.product.gpcash.corporate.corporatespecialrate.model.SpecialRateModel;
import com.gpt.product.gpcash.corporate.corporatespecialrate.repository.SpecialRateRepository;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.spi.CorporateUserGroupListener;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.cot.currency.services.CurrencyCOTService;
import com.gpt.product.gpcash.cot.system.services.SystemCOTService;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionValidationServiceImpl implements TransactionValidationService, CorporateUserGroupListener {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private SystemCOTService systemCOTService;
	
	@Autowired
	private CurrencyCOTService currencyCOTService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CalendarService calendarService;
	
	@Autowired
	private ExchangeRateRepository exchangeRateRepo;
	
	@Autowired
	private SpecialRateRepository specialRateRepo;
	
	@Override
	public void validateLimit(String userCode, String corporateId, String transactionServiceCode, String accountGroupDtlId,
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate, String instructionMode, Object recurringStartDate)
			throws ApplicationException, BusinessException {
		try {
			CorporateAccountGroupDetailModel corporateAccountGroupDetail = corporateUtilsRepo
					.isCorporateAccountGroupDetailIdValid(corporateId, accountGroupDtlId);

			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();
			
			//validate user maker Limit
			validateMakerLimit(corporateUser, transactionAmount);
			
			// validate account Limit
			validateAccountLimit(corporateAccountGroupDetail, transactionAmount);

			String sourceAccountCurrency = corporateAccountGroupDetail.getCorporateAccount().getAccount().getCurrency()
					.getCode();
			String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);

			Map<String, Object> trxInfoMap = null;
			
			//if recurring maka instructionDate di ambil dari recurringStartDate
			if(!(instructionMode).equals(ApplicationConstants.SI_RECURRING)) {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) instructionDate, instructionMode, corporateUserGroup.getId());
			} else {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) recurringStartDate, instructionMode, corporateUserGroup.getId());
			}
			
			int totalPendingTaskTrxOccurence = ((Number) trxInfoMap.get("totalPendingTaskTrxOccurence")).intValue();
			BigDecimal totalPendingTaskDebitedAmount = (BigDecimal) trxInfoMap.get("totalPendingTaskDebitedAmount");
			
			// validate bank transaction limit
			validateBankTransactionLimit(transactionServiceCode, currencyMatrixCode, transactionCurrency,
					transactionAmount, applicationCode);

			// validate corporate limit
			validateCorporateLimit(corporateId, transactionServiceCode, currencyMatrixCode, transactionCurrency, transactionAmount,
					applicationCode, totalPendingTaskTrxOccurence, totalPendingTaskDebitedAmount);

			// validate user group limit
			validateUserGroupLimit(corporateId, transactionServiceCode, currencyMatrixCode, corporateUserGroup.getId(), transactionAmount, totalPendingTaskDebitedAmount);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void validateMakerLimit(CorporateUserModel corporateUser,
			BigDecimal transactionAmount) throws BusinessException {
		
		BigDecimal makerLimit = corporateUser.getAuthorizedLimit().getMakerLimit();
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateUser.getId() : " + corporateUser.getId());
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("makerLimit : " + makerLimit);
			logger.debug("\n\n\n\n\n");
		}
		
		if(transactionAmount.compareTo(corporateUser.getAuthorizedLimit().getMakerLimit()) > 0) {
			throw new BusinessException("GPT-0100184");
		}
	}

	private void validateAccountLimit(CorporateAccountGroupDetailModel corporateAccountGroupDetail,
			BigDecimal transactionAmount) throws BusinessException {
		CorporateAccountModel corporateAccount = corporateAccountGroupDetail.getCorporateAccount();

		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateAccountGroupDetail.getIsDebit() : " + corporateAccountGroupDetail.getIsDebit());
			logger.debug("corporateAccount.getDebitLimit() : " + corporateAccount.getDebitLimit());
			logger.debug("\n\n\n\n\n");
		}
		
		// validate account limit flag must Y
		if ((corporateAccountGroupDetail.getIsDebit().equals(ApplicationConstants.YES))
				&& (corporateAccount.getDebitLimit() != null)
				&& (transactionAmount.compareTo(corporateAccount.getDebitLimit()) > 0)) {
			throw new BusinessException("GPT-0100091");
		} else if(corporateAccountGroupDetail.getIsDebit().equals(ApplicationConstants.NO)){ // validate debitFlag flag must Y
			throw new BusinessException("GPT-0100092");
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

	private void validateCorporateLimit(String corporateId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode, int totalPendingTaskTrxOccurence, BigDecimal totalPendingTaskDebitedAmount) throws Exception {

		CorporateLimitModel corporateLimit = corporateUtilsRepo
				.isCorporateLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, corporateId);

		// TODO implement get equivalent amount for cross forex transaction

		int totalOccurence = corporateLimit.getOccurrenceLimitUsage() + totalPendingTaskTrxOccurence;
		BigDecimal totalDebitedAmount = corporateLimit.getAmountLimitUsage().add(totalPendingTaskDebitedAmount);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateId : " + corporateId);
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionCurrency : " + transactionCurrency);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("totalPendingTaskTrxOccurence : " + totalPendingTaskTrxOccurence);
			logger.debug("totalPendingTaskDebitedAmount : " + totalPendingTaskDebitedAmount);
			logger.debug("totalOccurence : " + totalOccurence);
			logger.debug("corporateLimit.getMaxOccurrenceLimit() : " + corporateLimit.getMaxOccurrenceLimit());
			logger.debug("corporateLimit.getAmountLimitUsage() : " + corporateLimit.getAmountLimitUsage());
			logger.debug("totalDebitedAmount : " + totalDebitedAmount);
			logger.debug("corporateLimit.getMaxAmountLimit( : " + corporateLimit.getMaxAmountLimit());
			logger.debug("\n\n\n\n\n");
		}

		if (totalOccurence + 1 > corporateLimit.getMaxOccurrenceLimit()) {
			throw new BusinessException("GPT-0100095");
		}

		if (totalDebitedAmount.add(transactionAmount).compareTo(corporateLimit.getMaxAmountLimit()) > 0) {
			throw new BusinessException("GPT-0100096");
		}
	}

	public Map<String, Object> getTrxInfoFromPendingTask(String serviceCode, Timestamp instructionDate, String instructionMode, String userGroupId)
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
				pendingTaskStatus, userGroupId);

		trxInfoMap.put("totalPendingTaskTrxOccurence", totals[0]);
		trxInfoMap.put("totalPendingTaskDebitedAmount", totals[1] == null ? new BigDecimal(0) : totals[1]);
		return trxInfoMap;
	}

	private void validateUserGroupLimit(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId, 
			BigDecimal transactionAmount, BigDecimal totalPendingTaskDebitedAmount) throws Exception {
		CorporateUserGroupDetailModel corporateUserGroupDetail = corporateUtilsRepo
				.isCorporateUserGroupDetailValid(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId);

		// TODO implement get equivalent amount for cross forex transaction

		BigDecimal totalDebitedAmount = corporateUserGroupDetail.getAmountLimitUsage().add(totalPendingTaskDebitedAmount);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateId : " + corporateId);
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("corporateUserGroupDetail.getAmountLimitUsage() : " + corporateUserGroupDetail.getAmountLimitUsage());
			logger.debug("totalPendingTaskDebitedAmount : " + totalPendingTaskDebitedAmount);
			logger.debug("totalDebitedAmount : " + totalDebitedAmount);
			logger.debug("corporateUserGroupDetail.getMaxAmountLimit() : " + corporateUserGroupDetail.getMaxAmountLimit());
			logger.debug("\n\n\n\n\n");
		}
		
		if (totalDebitedAmount.add(transactionAmount).compareTo(corporateUserGroupDetail.getMaxAmountLimit()) > 0) {
			throw new BusinessException("GPT-0100094");
		}
	}

	private String getCurrencyMatrixCode(String fromCurrency, String toCurrency)
			throws Exception {
		String localCurrency = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
		
		if (fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_LL;
		} else if (fromCurrency.equalsIgnoreCase(localCurrency) 
				&& !toCurrency.equalsIgnoreCase(fromCurrency)) {
			return ApplicationConstants.CCY_MTRX_LF;
		} else if (toCurrency.equalsIgnoreCase(localCurrency) 
				&& !toCurrency.equalsIgnoreCase(fromCurrency)) {
			return ApplicationConstants.CCY_MTRX_FL;
		}else if (!fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
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
	public void validateChargeAndTotalTransaction(String corporateId, String transactionServiceCode, BigDecimal transactionAmount,
			BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode, List<Map<String, Object>> chargeList, String chargeInstruction) throws ApplicationException, BusinessException {
		try{
			//validate charging & totalDebitedAmount
			Map<String, Object> chargesInfo =  corporateChargeService.getCorporateCharges(applicationCode,
					transactionServiceCode, corporateId);
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get("totalCharge"));
			BigDecimal totalTransaction = transactionAmount.add(totalChargeFromTable);
			if(ApplicationConstants.BENEFICIARY_CHARGES_INSTRUCTION.equals(chargeInstruction)) {
				totalTransaction = transactionAmount;
			}
			
			logger.debug("\n\n\n\n\n");
			logger.debug("totalChargeFromTable = " + totalChargeFromTable);
			logger.debug("totalTransaction = " + totalTransaction);
			logger.debug("\n\n\n\n\n");
			
			if(totalCharge.compareTo(totalChargeFromTable) != 0 || totalTransaction.compareTo(totalDebitedAmount) != 0){
				throw new BusinessException("GPT-0100107");
			}
			
			//check if charge id and value valid
			for(Map<String, Object> chargeMap : chargeList){
				CorporateChargeModel corporateCharge = corporateUtilsRepo.isCorporateChargeValid((String) chargeMap.get("id"));
				
				logger.debug("\n\n\n\n\n");
				logger.debug("corporateCharge.getValue() = " + corporateCharge.getValue());
				logger.debug("chargeMap.get(value) = " + (BigDecimal) chargeMap.get("value"));
				logger.debug("\n\n\n\n\n");
				
				if(corporateCharge.getValue().compareTo((BigDecimal) chargeMap.get("value")) != 0){
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
	public void validateChargeAndTotalTransactionPerRecords(String corporateId, String transactionServiceCode, BigDecimal transactionAmount,
			BigDecimal totalDebitedAmount, BigDecimal totalCharge, String applicationCode, List<Map<String, Object>> chargeList, Long records) throws ApplicationException, BusinessException {
		try{
			//validate charging & totalDebitedAmount
			Map<String, Object> chargesInfo =  corporateChargeService.getCorporateChargesPerRecord(applicationCode,
					transactionServiceCode, corporateId, records);
			
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
				CorporateChargeModel corporateCharge = corporateUtilsRepo.isCorporateChargeValid((String) chargeMap.get("id"));
				
				logger.debug("\n\n\n\n\n");
				logger.debug("corporateCharge.getValue() = " + corporateCharge.getValue());
				logger.debug("chargeMap.get(value) = " + (BigDecimal) chargeMap.get("value"));
				logger.debug("\n\n\n\n\n");
				
				if(corporateCharge.getValue().compareTo((BigDecimal) chargeMap.get("value")) != 0){
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
	public void validateWorkingDay(String corporateId, Date transactionDate)
			throws ApplicationException, BusinessException {
		
	}
	
	@Override
	public Timestamp getInstructionDateForRelease(String instructionMode, Timestamp instructionDate) throws Exception{
		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			String immediateType = maintenanceRepo.isSysParamValid(SysParamConstants.IMMEDIATE_TYPE).getValue();
			
			if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_RELEASE)){
				instructionDate = DateUtils.getCurrentTimestamp();
			}
		}
		
		return instructionDate;
	}
	
	@Override
	public Timestamp validateExpiryDate(String instructionMode, Timestamp instructionDate) throws Exception {
		
		if(ApplicationConstants.SI_IMMEDIATE.equals(instructionMode)){
			String immediateType = maintenanceRepo.isSysParamValid(SysParamConstants.IMMEDIATE_TYPE).getValue();
			
			if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_CREATE)){
				return InstructionModeUtils.getNextSomeDayFromTimestamp(instructionDate, 1);
			}
		}
		
		return null;
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
				String immediateType = sysParamService.getValueByCode(SysParamConstants.IMMEDIATE_TYPE);
				if(isReleaseTrx || immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_CREATE)){
					systemCOTService.validateSystemCOT(serviceCode, applicationCode);
					
					if(isCheckCurrencyCOT)
						currencyCOTService.validateSystemCOT(currencyCode, applicationCode);
				}
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
			Timestamp recurringStartDate, Timestamp recurringEndDate, String sessionTime, boolean isRelease, String transactionCurrency)  throws ApplicationException, BusinessException {
		
		if(!isRelease) {
			if(ApplicationConstants.SI_RECURRING.equals(instructionMode)) {
				instructionDate = InstructionModeUtils.getStandingInstructionDate(recurringParamType, 
						recurringParam, 
						recurringStartDate);
			}
		}
		
		
		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			String immediateType = sysParamService.getValueByCode(SysParamConstants.IMMEDIATE_TYPE);
			
			if (immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_RELEASE)) {
				instructionDate = DateUtils.getCurrentTimestamp();
			}
		}
		
		if(calendarService.isHoliday(instructionDate)) {
			throw new BusinessException("GPT-0100200");
		}
		
		if (calendarService.isCurrencyHoliday(instructionDate, transactionCurrency)) {
			throw new BusinessException("GPT-0200019", new String[] {String.valueOf(transactionCurrency)});
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void updateTransactionLimit(String corporateId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, String corporateUserGroupId, BigDecimal transactionAmount, String applicationCode) throws Exception {
		
		String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);
		
		CorporateUserGroupDetailModel corporateUserGroupDetail = corporateUtilsRepo
				.isCorporateUserGroupDetailValid(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId);
		
		// locking here
		corporateUtilsRepo.getCorporateUserGroupRepo().findByIdWithLocking(corporateUserGroupId);
		
		updateUserGroupLimit(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId, transactionAmount, corporateUserGroupDetail.getId());
		
		updateCorporateLimit(corporateId, serviceCode, currencyMatrixCode, transactionCurrency, transactionAmount, applicationCode);		
	}

	private void updateUserGroupLimit(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId, 
			BigDecimal transactionAmount, String corporateUserGroupDetailId) throws Exception {
		
		int ret = corporateUtilsRepo.getCorporateUserGroupRepo().updateUserGroupLimit(transactionAmount, corporateUserGroupDetailId);
		if (ret==0)
			throw new BusinessException("GPT-0100094");
	}
	
	private void updateCorporateLimit(String corporateId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode) throws Exception {

		CorporateLimitModel corporateLimit = corporateUtilsRepo
				.isCorporateLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, corporateId);
		
		if (corporateLimit.getOccurrenceLimitUsage() + 1 > corporateLimit.getMaxOccurrenceLimit())
			throw new BusinessException("GPT-0100095");

		if (corporateLimit.getAmountLimitUsage().add(transactionAmount).compareTo(corporateLimit.getMaxAmountLimit()) > 0)
			throw new BusinessException("GPT-0100096");

		corporateUtilsRepo.getCorporateLimitRepo().updateCorporateLimit(transactionAmount, corporateLimit.getId());
	}	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void reverseUpdateTransactionLimit(String corporateId, String serviceCode, String sourceAccountCurrency,
			String transactionCurrency, String corporateUserGroupId, BigDecimal transactionAmount, String applicationCode) throws Exception {
		
		String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);
		
		CorporateUserGroupDetailModel corporateUserGroupDetail = corporateUtilsRepo
				.isCorporateUserGroupDetailValid(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId);
		
		// locking here
		corporateUtilsRepo.getCorporateUserGroupRepo().findByIdWithLocking(corporateUserGroupId);
		
		reverseUpdateUserGroupLimit(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId, transactionAmount, corporateUserGroupDetail.getId());
		
		reverseUpdateCorporateLimit(corporateId, serviceCode, currencyMatrixCode, transactionCurrency, transactionAmount, applicationCode);
	}
	
	private void reverseUpdateUserGroupLimit(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId, 
			BigDecimal transactionAmount, String corporateUserGroupDetailId) throws Exception {
		
		corporateUtilsRepo.getCorporateUserGroupRepo().reverseUpdateUserGroupLimit(transactionAmount, corporateUserGroupDetailId);
	}	
	
	private void reverseUpdateCorporateLimit(String corporateId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode) throws Exception {

		CorporateLimitModel corporateLimit = corporateUtilsRepo
				.isCorporateLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, corporateId);
		
		corporateUtilsRepo.getCorporateLimitRepo().reverseUpdateCorporateLimit(transactionAmount, corporateLimit.getId());
	}

	@Override
	public void validateMaximumRecordBulkTransaction(int totalRecord) throws ApplicationException, BusinessException {
		try {			
			int maxRecords = Integer.valueOf(maintenanceRepo.isSysParamValid(SysParamConstants.MAX_RECORD_BULK_TRANSACTION).getValue());
			if (totalRecord > maxRecords)
				throw new BusinessException("GPT-0100204", new String[] {String.valueOf(maxRecords)});
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void validateMinimumPayment(BigDecimal transactionAmount,BigDecimal minimumPayment) throws ApplicationException, BusinessException {
		
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("minimumPayment : " + minimumPayment);
			logger.debug("\n\n\n\n\n");
		}
		
		if(transactionAmount.compareTo(minimumPayment) < 0) {
			throw new BusinessException("GPT-0100228");
		}
	}

	
	private Map<String, Object> getLatestExchangeRate() throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ExchangeRateModel exchangeRate;
		List<ExchangeRateModel> rateList = exchangeRateRepo.findByDeleteFlag(ApplicationConstants.NO);
		
		for (int i = 0; i < rateList.size(); i++) {
			exchangeRate = rateList.get(i);
			returnMap.put(exchangeRate.getCurrency().getCode(), exchangeRate);
		}
		
		return returnMap;
	}

	private BigDecimal calculateAmountEquivalentForLimit(String limitCurrency, String transactionCurrency,
			String localCurrency, BigDecimal transactionAmount, String currencyMatrix, Map<String, Object> rate)
			throws Exception {
		
		if (limitCurrency.equals(transactionCurrency)) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("transactionAmountEquivalent = " + transactionAmount);
			}
			
			return transactionAmount;
		} else {
			BigDecimal trxAmountEquivalent = ApplicationConstants.ZERO;
			ExchangeRateModel exchangeRateFrom;
			BigDecimal sellRate = ApplicationConstants.ZERO;
			BigDecimal midRate = ApplicationConstants.ZERO;
			BigDecimal buyRate = ApplicationConstants.ZERO;
			BigDecimal currencyQuantity = new BigDecimal(1);
			
			if (!transactionCurrency.equals(localCurrency) && !limitCurrency.equals(localCurrency)) {
				BigDecimal trxIntermmediate;
				exchangeRateFrom = getRate(transactionCurrency, rate);
				sellRate = exchangeRateFrom.getTransactionSellRate();
				midRate = exchangeRateFrom.getTransactionMidRate();
				
				if (currencyMatrix.equals(ApplicationConstants.CCY_MTRX_FS)) {
					if (midRate.compareTo(ApplicationConstants.ZERO) == 0) {
						throw new BusinessException("Rate Not Found");
					}
					trxIntermmediate = transactionAmount.multiply(midRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
				} else {
					if (sellRate.compareTo(ApplicationConstants.ZERO) == 0) {
						throw new BusinessException("Rate Not Found");
					}
					trxIntermmediate = transactionAmount.multiply(sellRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("trxIntermmediate = " + trxIntermmediate);
				}
				
				exchangeRateFrom = getRate(limitCurrency, rate);
				buyRate = exchangeRateFrom.getTransactionBuyRate();
				midRate = exchangeRateFrom.getTransactionMidRate();
				
				if (currencyMatrix.equals(ApplicationConstants.CCY_MTRX_FS)) {
					if (midRate.compareTo(ApplicationConstants.ZERO) == 0) {
						throw new BusinessException("Rate Not Found");
					}
					trxAmountEquivalent = trxIntermmediate.divide(midRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP).multiply(currencyQuantity);
				} else {
					if (buyRate.compareTo(ApplicationConstants.ZERO) == 0) {
						throw new BusinessException("Rate Not Found");
					}
					trxAmountEquivalent = trxIntermmediate.divide(buyRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP).multiply(currencyQuantity);
				}
				
			} else {
				
				if (limitCurrency.equals(localCurrency)) {
					exchangeRateFrom = getRate(transactionCurrency, rate);
				} else {
					exchangeRateFrom = getRate(limitCurrency, rate);
				}
				
				sellRate = exchangeRateFrom.getTransactionSellRate();
				midRate = exchangeRateFrom.getTransactionMidRate();
				buyRate = exchangeRateFrom.getTransactionBuyRate();
				
				if (limitCurrency.equals(localCurrency)) {
					if (currencyMatrix.equals(ApplicationConstants.CCY_MTRX_LL) || currencyMatrix.equals(ApplicationConstants.CCY_MTRX_FS)) {
						if (midRate.compareTo(ApplicationConstants.ZERO) == 0) {
							throw new BusinessException("Rate Not Found");
						}
						trxAmountEquivalent = transactionAmount.multiply(midRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
					} else {
						if (sellRate.compareTo(ApplicationConstants.ZERO) == 0) {
							throw new BusinessException("Rate Not Found");
						}
						trxAmountEquivalent = transactionAmount.multiply(sellRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
					}
				} else if (transactionCurrency.equals(localCurrency)) {
					if (currencyMatrix.equals(ApplicationConstants.CCY_MTRX_LL) || currencyMatrix.equals(ApplicationConstants.CCY_MTRX_FS)) {
						if (midRate.compareTo(ApplicationConstants.ZERO) == 0) {
							throw new BusinessException("Rate Not Found");
						}
						trxAmountEquivalent = transactionAmount.divide(midRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP).multiply(currencyQuantity);
					} else {
						if (buyRate.compareTo(ApplicationConstants.ZERO) == 0) {
							throw new BusinessException("Rate Not Found");
						}
						trxAmountEquivalent = transactionAmount.divide(buyRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP).multiply(currencyQuantity);
					}
				}
				
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("transactionAmountEquivalent = " + trxAmountEquivalent);
			}
			
			return trxAmountEquivalent;
		}
	}

	
	private BigDecimal calculateAmountEquivalent(String transactionCurrency, String accountCurrency, String localCurrency, 
			BigDecimal transactionAmount, Map<String, Object> rate) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("transactionCurrency = " + transactionCurrency);
			logger.debug("transactionAmount = " + transactionAmount);
			logger.debug("accountCurrency = " + accountCurrency);
			logger.debug("localCurrency" + localCurrency);
		}
		
		BigDecimal trxAmountEquivalent = ApplicationConstants.ZERO;
		BigDecimal sellRate = ApplicationConstants.ZERO;
		BigDecimal buyRate = ApplicationConstants.ZERO;
		BigDecimal currencyQuantity = new BigDecimal(1);
		ExchangeRateModel exchangeRateFrom;
		
		if(!accountCurrency.equals(transactionCurrency)){
			
			if (!accountCurrency.equals(localCurrency) && !transactionCurrency.equals(localCurrency)) {
				
				exchangeRateFrom = getRate(transactionCurrency, rate);
				sellRate = exchangeRateFrom.getTransactionSellRate();
				
				BigDecimal trxIntermmediate = transactionAmount.multiply(sellRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
				
				exchangeRateFrom = getRate(accountCurrency, rate);
				buyRate = exchangeRateFrom.getTransactionBuyRate();
				
				trxAmountEquivalent = trxIntermmediate.divide(buyRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
				trxAmountEquivalent = trxAmountEquivalent.multiply(currencyQuantity);
			} else {
				
				if (accountCurrency.equals(localCurrency)) {
					
					exchangeRateFrom = getRate(transactionCurrency, rate);
					sellRate = exchangeRateFrom.getTransactionSellRate();
					trxAmountEquivalent = transactionAmount.multiply(sellRate).divide(currencyQuantity, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
					
				} else if (transactionCurrency.equals(localCurrency)) {
					
					exchangeRateFrom = getRate(accountCurrency, rate);
					buyRate = exchangeRateFrom.getTransactionBuyRate();
					trxAmountEquivalent = transactionAmount.multiply(currencyQuantity).divide(buyRate, ApplicationConstants.ROUND_SCALE, ApplicationConstants.ROUND_MODE_UP);
				}
			}
		} else {
			trxAmountEquivalent = transactionAmount;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("transactionAmountEquivalent = " + trxAmountEquivalent);
		}
		
		return trxAmountEquivalent;
	}

	private ExchangeRateModel getRate(String currency, Map<String, Object> rate) throws BusinessException {
		ExchangeRateModel returnRate = new ExchangeRateModel();
		returnRate = (ExchangeRateModel) rate.get(currency);
		if (returnRate == null) {
			throw new BusinessException("Rate Not Found");
		}
		return returnRate;
	}

	@Override
	public Map<String, Object> validateLimitEquivalent(String userCode, String corporateId, String transactionServiceCode, String accountGroupDtlId, 
			BigDecimal transactionAmount, String transactionCurrency, String applicationCode, Object instructionDate, String instructionMode,
			Object recurringStartDate, String exchangeRate, String treasuryCode) throws ApplicationException, BusinessException {
		
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			CorporateAccountGroupDetailModel corporateAccountGroupDetail = corporateUtilsRepo
					.isCorporateAccountGroupDetailIdValid(corporateId, accountGroupDtlId);

			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();
			
			String localCurrency = sysParamService.getLocalCurrency();
			String sourceAccountCurrency = corporateAccountGroupDetail.getCorporateAccount().getAccount().getCurrency().getCode();
			String currencyMatrixCode = getCurrencyMatrixCode(sourceAccountCurrency, transactionCurrency);	
			Map<String, Object> rateMap = getLatestExchangeRate();
			BigDecimal equivalentAmount = BigDecimal.ZERO;
			if (exchangeRate.equals(ApplicationConstants.RATE_COUNTER)) {
				equivalentAmount = calculateAmountEquivalent(transactionCurrency, sourceAccountCurrency, localCurrency, transactionAmount, rateMap);
			}else {
				Map<String, Object> specialRateMap = checkSpecialRate(treasuryCode, sourceAccountCurrency, transactionCurrency, transactionAmount, (Timestamp) instructionDate, corporateId, instructionMode);
				equivalentAmount = (BigDecimal) specialRateMap.get("equivalentAmount");
			}
			returnMap.put("rateMap", rateMap);
			returnMap.put("equivalentAmount", equivalentAmount);
			returnMap.put("sourceAccountCurrency", sourceAccountCurrency);
			
			//validate user maker Limit
			validateMakerLimit(corporateUser, equivalentAmount);
			
			// validate account Limit
			validateAccountLimit(corporateAccountGroupDetail, equivalentAmount);


			Map<String, Object> trxInfoMap = null;
			
			//if recurring maka instructionDate di ambil dari recurringStartDate
			if(!(instructionMode).equals(ApplicationConstants.SI_RECURRING)) {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) instructionDate, instructionMode, corporateUserGroup.getId());
			} else {
				trxInfoMap = getTrxInfoFromPendingTask(transactionServiceCode, (Timestamp) recurringStartDate, instructionMode, corporateUserGroup.getId());
			}
			
			int totalPendingTaskTrxOccurence = ((Number) trxInfoMap.get("totalPendingTaskTrxOccurence")).intValue();
			BigDecimal totalPendingTaskDebitedAmount = (BigDecimal) trxInfoMap.get("totalPendingTaskDebitedAmount");
			
			// validate bank transaction limit
			validateBankTransactionLimitEquivalent(transactionServiceCode, currencyMatrixCode, transactionCurrency,
					transactionAmount, applicationCode, localCurrency, rateMap,exchangeRate,treasuryCode,instructionDate,instructionMode,corporateId);

			// validate corporate limit
			validateCorporateLimitEquivalent(corporateId, transactionServiceCode, currencyMatrixCode, transactionCurrency, transactionAmount,
					applicationCode, totalPendingTaskTrxOccurence, totalPendingTaskDebitedAmount, localCurrency, rateMap);

			// validate user group limit
			validateUserGroupLimitEquivalent(corporateId, transactionServiceCode, currencyMatrixCode, corporateUserGroup.getId(), transactionAmount, totalPendingTaskDebitedAmount, localCurrency, rateMap, transactionCurrency);
			
			//validate bankforex limit
			if(!currencyMatrixCode.equals(ApplicationConstants.CCY_MTRX_LL)) {
				validateBankForexLimitEquivalent(transactionServiceCode, currencyMatrixCode, sourceAccountCurrency, transactionAmount, applicationCode, transactionCurrency, rateMap);
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
		
		return returnMap;
		
	}
	
	private void validateBankTransactionLimitEquivalent(String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode, String localCurrency, Map<String, Object> rateMap, 
			String exchangeRate, String treasuryCode,Object instructionDate, String instructionMode,String corporateId) throws Exception {
		
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

		BigDecimal amountEquivalent;
		
		if (bankTransactionLimit != null) {
			//if (exchangeRate.equals(ApplicationConstants.RATE_COUNTER)) {
				amountEquivalent = calculateAmountEquivalentForLimit(bankTransactionLimit.getCurrency().getCode(), transactionCurrency, localCurrency, transactionAmount, currencyMatrixCode, rateMap);
			/*}else {
				Map<String, Object> specialRateMap = checkSpecialRate(treasuryCode,transactionCurrency, bankTransactionLimit.getCurrency().getCode(), transactionAmount, (Timestamp) instructionDate, corporateId, instructionMode);
				amountEquivalent = (BigDecimal) specialRateMap.get("equivalentAmount");
			}*/
			
			
			
			if (amountEquivalent.compareTo(bankTransactionLimit.getMinAmountLimit()) < 0
					|| amountEquivalent.compareTo(bankTransactionLimit.getMaxAmountLimit()) > 0) {
				throw new BusinessException("GPT-0100093");
			}
		}
	}
	
	private void validateBankForexLimitEquivalent(String serviceCode, String currencyMatrixCode, String sourceAccountCurrency,
			BigDecimal transactionAmount, String applicationCode, String transactionCurrency, Map<String, Object> rateMap) throws Exception {

			BigDecimal amountEquivalent = transactionAmount;
		
			//amountEquivalent = calculateAmountEquivalentForLimit(localCurrency, transactionCurrency, localCurrency, transactionAmount, currencyMatrixCode, rateMap);
			
			if(currencyMatrixCode.equals(ApplicationConstants.CCY_MTRX_LF)) {
				BankForexLimitModel bankForexLimit = productRepo.isBankForexLimitValid(transactionCurrency);
				
				if(logger.isDebugEnabled()) {
					logger.debug("\n\n\n\n\n");
					logger.debug("sourceAccountCurrency : " + sourceAccountCurrency);
					logger.debug("transactionCurrency : " + transactionCurrency);
					logger.debug("transactionAmount : " + transactionAmount);
					logger.debug("bankForexLimit.getMinBuyLimit() : " + bankForexLimit.getMinBuyLimit());
					logger.debug("bankForexLimit.getMaxBuyLimit() : " + bankForexLimit.getMaxBuyLimit());
					logger.debug("bankForexLimit.getMinSellLimit() : " + bankForexLimit.getMinSellLimit());
					logger.debug("bankForexLimit.getMaxSellLimit() : " + bankForexLimit.getMaxSellLimit());
					logger.debug("\n\n\n\n\n");
				}
				
				if (amountEquivalent.compareTo(bankForexLimit.getMinSellLimit()) < 0
						|| amountEquivalent.compareTo(bankForexLimit.getMaxSellLimit().add(bankForexLimit.getLimitUsageEquivalent())) > 0) {
					throw new BusinessException("GPT-131194"); 
				}	
			}else if (currencyMatrixCode.equals(ApplicationConstants.CCY_MTRX_FL)) {
				BankForexLimitModel bankForexLimit = productRepo.isBankForexLimitValid(sourceAccountCurrency);
				
				if(logger.isDebugEnabled()) {
					logger.debug("\n\n\n\n\n");
					logger.debug("sourceAccountCurrency : " + sourceAccountCurrency);
					logger.debug("transactionCurrency : " + transactionCurrency);
					logger.debug("transactionAmount : " + transactionAmount);
					logger.debug("bankForexLimit.getMinBuyLimit() : " + bankForexLimit.getMinBuyLimit());
					logger.debug("bankForexLimit.getMaxBuyLimit() : " + bankForexLimit.getMaxBuyLimit());
					logger.debug("bankForexLimit.getMinSellLimit() : " + bankForexLimit.getMinSellLimit());
					logger.debug("bankForexLimit.getMaxSellLimit() : " + bankForexLimit.getMaxSellLimit());
					logger.debug("\n\n\n\n\n");
				}
				
				if (amountEquivalent.compareTo(bankForexLimit.getMinBuyLimit()) < 0
						|| amountEquivalent.compareTo(bankForexLimit.getMaxBuyLimit().subtract(bankForexLimit.getLimitUsageEquivalent())) > 0) {
					throw new BusinessException("GPT-131195"); 
				}	
			}			
		
	}
	
	private void validateCorporateLimitEquivalent(String corporateId, String serviceCode, String currencyMatrixCode, String transactionCurrency,
			BigDecimal transactionAmount, String applicationCode, int totalPendingTaskTrxOccurence, BigDecimal totalPendingTaskDebitedAmount, String localCurrency, Map<String, Object> rateMap) throws Exception {

		CorporateLimitModel corporateLimit = corporateUtilsRepo
				.isCorporateLimitValid(serviceCode, currencyMatrixCode, applicationCode, transactionCurrency, corporateId);

		// TODO implement get equivalent amount for cross forex transaction

		int totalOccurence = corporateLimit.getOccurrenceLimitUsage() + totalPendingTaskTrxOccurence;
		BigDecimal totalDebitedAmount = corporateLimit.getAmountLimitUsage().add(totalPendingTaskDebitedAmount);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateId : " + corporateId);
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionCurrency : " + transactionCurrency);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("totalPendingTaskTrxOccurence : " + totalPendingTaskTrxOccurence);
			logger.debug("totalPendingTaskDebitedAmount : " + totalPendingTaskDebitedAmount);
			logger.debug("totalOccurence : " + totalOccurence);
			logger.debug("corporateLimit.getCurrency().getCode() : " + corporateLimit.getCurrency().getCode());
			logger.debug("corporateLimit.getMaxOccurrenceLimit() : " + corporateLimit.getMaxOccurrenceLimit());
			logger.debug("corporateLimit.getAmountLimitUsage() : " + corporateLimit.getAmountLimitUsage());
			logger.debug("totalDebitedAmount : " + totalDebitedAmount);
			logger.debug("corporateLimit.getMaxAmountLimit( : " + corporateLimit.getMaxAmountLimit());
			logger.debug("\n\n\n\n\n");
		}
		
		BigDecimal amountEquivalent = calculateAmountEquivalentForLimit(corporateLimit.getCurrency().getCode(), transactionCurrency, localCurrency, transactionAmount, currencyMatrixCode, rateMap);

		if (totalOccurence + 1 > corporateLimit.getMaxOccurrenceLimit()) {
			throw new BusinessException("GPT-0100095");
		}

		if (totalDebitedAmount.add(amountEquivalent).compareTo(corporateLimit.getMaxAmountLimit()) > 0) {
			throw new BusinessException("GPT-0100096");
		}
	}
	
	private void validateUserGroupLimitEquivalent(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId, 
			BigDecimal transactionAmount, BigDecimal totalPendingTaskDebitedAmount, String localCurrency, Map<String, Object> rateMap, String transactionCurrency) throws Exception {
		CorporateUserGroupDetailModel corporateUserGroupDetail = corporateUtilsRepo
				.isCorporateUserGroupDetailValid(corporateId, serviceCode, currencyMatrixCode, corporateUserGroupId);

		// TODO implement get equivalent amount for cross forex transaction

		BigDecimal totalDebitedAmount = corporateUserGroupDetail.getAmountLimitUsage().add(totalPendingTaskDebitedAmount);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("corporateId : " + corporateId);
			logger.debug("serviceCode : " + serviceCode);
			logger.debug("currencyMatrixCode : " + currencyMatrixCode);
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("corporateUserGroupDetail.getAmountLimitUsage() : " + corporateUserGroupDetail.getAmountLimitUsage());
			logger.debug("totalPendingTaskDebitedAmount : " + totalPendingTaskDebitedAmount);
			logger.debug("totalDebitedAmount : " + totalDebitedAmount);
			logger.debug("corporateUserGroupDetail.getMaxAmountLimit() : " + corporateUserGroupDetail.getMaxAmountLimit());
			logger.debug("corporateUserGroupDetail.getCurrency().getCode() : " + corporateUserGroupDetail.getCurrency().getCode());
			logger.debug("\n\n\n\n\n");
		}
		
		BigDecimal amountEquivalent = calculateAmountEquivalentForLimit(corporateUserGroupDetail.getCurrency().getCode(), transactionCurrency, localCurrency, transactionAmount, currencyMatrixCode, rateMap);
		
		if (totalDebitedAmount.add(amountEquivalent).compareTo(corporateUserGroupDetail.getMaxAmountLimit()) > 0) {
			throw new BusinessException("GPT-0100094");
		}
	}

	@Override
	public Map<String, Object> validateCounterRate(String sourceAccountCurrency, String transactionCurrency, BigDecimal transactionAmount) throws ApplicationException, BusinessException {
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			
			String localCurrency = sysParamService.getLocalCurrency();
			Map<String, Object> rateMap = getLatestExchangeRate();
			BigDecimal equivalentAmount = calculateAmountEquivalent(transactionCurrency, sourceAccountCurrency, localCurrency, transactionAmount, rateMap);
			ExchangeRateModel exchangeRate = getRate(sourceAccountCurrency, rateMap);
			
			returnMap.put("equivalentAmount", equivalentAmount);
			returnMap.put("trxBuyRate", ValueUtils.getValue(exchangeRate.getTransactionBuyRate()));
			returnMap.put("validDate", exchangeRate.getUpdatedDate() !=null ? Helper.DATE_TIME_FORMATTER.format(exchangeRate.getUpdatedDate()) : Helper.DATE_TIME_FORMATTER.format(exchangeRate.getCreatedDate()));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
		
		return returnMap;
	}

	@Override
	public Map<String, Object> checkSpecialRate(String treasuryCode, String sourceAccountCurrency,
			String transactionCurrency, BigDecimal transactionAmount, Timestamp instructionDate,
			String corporateId,String instructionMode)
			throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			
			// preparing input
			Map<String, Object> map = new HashMap<>();
			map.put("refNoSpecialRate", treasuryCode);
			map.put("dateFrom", new Date(instructionDate.getTime()));
			map.put("dateTo", new Date(instructionDate.getTime()));			
			map.put("corporateId", corporateId);
			map.put("status", ApplicationConstants.SPECIAL_RATE_OPEN);
			map.put("instructionMode",instructionMode);
			
			
			String localCurrency = sysParamService.getLocalCurrency();						
			if(localCurrency.equals(transactionCurrency)) { //localSpecialRate BuyRate
				map.put("rateType", "LOCAL");
				map.put("foreignCurrency1",sourceAccountCurrency);
				SpecialRateModel specialRateModel =  getSpecialRate(map);
				if(specialRateModel.getTransactionBuyRate()!=null) {
					if(specialRateModel.getTransactionAmountRate().compareTo(transactionAmount)==0) {
						returnMap.put("equivalentAmount", specialRateModel.getBuyAmountRate());
						returnMap.put("specialRate", sourceAccountCurrency +" "+ specialRateModel.getTransactionBuyRate());
						returnMap.put("validDate", Helper.DATE_TIME_FORMATTER.format(specialRateModel.getCreatedDate()));
					}else {
						throw new BusinessException("GPT-131191"); //invalid special rate
					}
					
				}else {
					throw new BusinessException("GPT-131191"); //invalid special rate
				}
				
			}else {
				if(localCurrency.equals(sourceAccountCurrency)) { //localSpecialRate SellRate
					map.put("rateType", "LOCAL");
					map.put("foreignCurrency1",transactionCurrency);
					SpecialRateModel specialRateModel =  getSpecialRate(map);
					if(specialRateModel.getTransactionSellRate()!=null) {
						if(specialRateModel.getTransactionAmountRate().compareTo(transactionAmount)==0) {
							returnMap.put("equivalentAmount", specialRateModel.getSellAmountRate());
							returnMap.put("specialRate", transactionCurrency +" "+ specialRateModel.getTransactionSellRate());
							returnMap.put("validDate", Helper.DATE_TIME_FORMATTER.format(specialRateModel.getCreatedDate()));
						}else {
							throw new BusinessException("GPT-131191"); //invalid special rate
						}
						
					}else {
						throw new BusinessException("GPT-131191"); //invalid special rate
					}
					
				}else { //foreignSpecialRate cross currency
					map.put("rateType", "FOREIGN");
					map.put("foreignCurrency1", sourceAccountCurrency);
					map.put("foreignCurrency2", transactionCurrency);
					SpecialRateModel specialRateModel =  getSpecialRate(map);
					if(specialRateModel.getTransactionAmountRate() == transactionAmount) {
						// 1. bank buy foreigncurrency1/sourceaccoutncurrency to IDR
						BigDecimal eqAmount = transactionAmount.multiply(specialRateModel.getTransactionBuyRate());
						
						//2. bank sell foreigncurrency 2/trasactionCurrency from IDR
						eqAmount = eqAmount.divide(specialRateModel.getTransactionSellRate());
						
						returnMap.put("equivalentAmount", eqAmount);
						returnMap.put("specialRate", sourceAccountCurrency +" "+ specialRateModel.getTransactionBuyRate()+" , "+transactionCurrency +" "+ specialRateModel.getTransactionSellRate());
						returnMap.put("validDate", Helper.DATE_TIME_FORMATTER.format(specialRateModel.getCreatedDate()));
						
					}else {
						throw new BusinessException("GPT-131191"); //invalid special rate
					}
					
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
		
		return returnMap;
	}

	private SpecialRateModel getSpecialRate(Map<String, Object> map) throws ApplicationException, BusinessException, Exception {
		// TODO Auto-generated method stub
				SpecialRateModel specialRate = null;
				String instructionMode = (String) map.get("instructionMode");
				List<SpecialRateModel> list = specialRateRepo.search(map, null).getContent();

				if(!ValueUtils.hasValue(list)){
						throw new BusinessException("GPT-131191"); //invalid special rate
				}else{
					specialRate = list.get(0);
				}
				
				// get pending task by reference
				CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByRefNoSpecialRateAndStatusIsPending((String) map.get("refNoSpecialRate"));

				if (pendingTask != null) {
					throw new BusinessException("GPT-131197"); //Treasury Code is already in use in pending transaction
				}
				
				
				if(specialRate.getType().equals("TODAY")) { // allow immdidate only
					if(!instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
						throw new BusinessException("GPT-131191"); //invalid special rate
					}
				}else {  //allow immidate and standing instruction
					if(instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
						throw new BusinessException("GPT-131191"); //invalid special rate
					}
				}
				return specialRate;
	}

	@Override
	public void updateBankForexLimit(String sourceAccoutnCurrency, String transactionCurrency, BigDecimal transactionAmount,
			BigDecimal totalDebitedEquivalentAmount) throws Exception {
		// TODO Auto-generated method stub
		String currencyMatrixCode = getCurrencyMatrixCode(sourceAccoutnCurrency, transactionCurrency);
		
			if(currencyMatrixCode.equals(ApplicationConstants.CCY_MTRX_LF)){
				productRepo.getBankForexLimitRepo().updateBankForexSellLimitUsage(transactionCurrency,transactionAmount,totalDebitedEquivalentAmount);
			}else if(currencyMatrixCode.equals(ApplicationConstants.CCY_MTRX_FL)) {
				productRepo.getBankForexLimitRepo().updateBankForexBuyLimitUsage(sourceAccoutnCurrency,transactionAmount,totalDebitedEquivalentAmount);
			}
		
	}
}
