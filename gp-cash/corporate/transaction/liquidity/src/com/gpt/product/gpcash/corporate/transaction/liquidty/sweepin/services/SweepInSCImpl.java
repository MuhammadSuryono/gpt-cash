package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepin.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubSubVariable;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.liquidty.constants.LiquidityConstants;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class SweepInSCImpl implements SweepInSC {

	@Autowired
	private SweepInService sweepInService;
	
	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;

	@SuppressWarnings("unchecked")
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_CURRENCY),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, type = BigDecimal.class), 
		@Variable(name = "isAutoSweepBack", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "sweepAmountType", options = {LiquidityConstants.SWEEP_AMOUNT_TYPE_FIXED, LiquidityConstants.SWEEP_AMOUNT_TYPE_PERCENTAGE}),
		@Variable(name = "subAccountList", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
			@SubVariable(name = "remainingAmount", type = BigDecimal.class, defaultValue = "0"),
			@SubVariable(name = "description", required = false)
		}),
		@Variable(name = "chargeList", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value", type = BigDecimal.class),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = "totalCharge", type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { LiquidityConstants.SRVC_GPT_LIQ_SWEEP_IN }), 
		@Variable(name = "instructionMode", options = {	
			ApplicationConstants.SI_IMMEDIATE, 
			ApplicationConstants.SI_FUTURE_DATE, 
			ApplicationConstants.SI_RECURRING
		}), 
		@Variable(name = "instructionDate", type = Timestamp.class, format = Format.DATE_TIME, required = false), 
		@Variable(name = "sessionTime", required = false),
		@Variable(name = "recurringParamType", required = false, options = { 
			ApplicationConstants.SI_RECURRING_TYPE_DAILY, 
			ApplicationConstants.SI_RECURRING_TYPE_WEEKLY, 
			ApplicationConstants.SI_RECURRING_TYPE_MONTHLY	
		}), 
		@Variable(name = "recurringParam", required = false, type = Integer.class, defaultValue = "0"),
		@Variable(name = "recurringStartDate", required = false, type = Timestamp.class), 
		@Variable(name = "recurringEndDate", required = false, type = Timestamp.class), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		
		transactionValidationService.validateChargeAndTotalTransactionPerRecords((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), BigDecimal.ZERO, 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT), (BigDecimal) map.get("totalCharge"), 
				(String)map.get(ApplicationConstants.APP_CODE), (ArrayList<Map<String,Object>>) map.get("chargeList"),Long.parseLong(String.valueOf(map.get("totalRecord"))));
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));
		
		Map<String, Object> resultMap = sweepInService.submit(map);
		
		globalTransactionService.updateCreatedTransactionByUserCode(userCode);
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { LiquidityConstants.SRVC_GPT_LIQ_SWEEP_IN }),
		@Variable(name = "subAccountList", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
			@SubVariable(name = "remainingAmount", type = BigDecimal.class, defaultValue = "0"),
			@SubVariable(name = "description", required = false)
		}),
		@Variable(name = "instructionMode", options = {
			ApplicationConstants.SI_IMMEDIATE, 
			ApplicationConstants.SI_FUTURE_DATE, 
			ApplicationConstants.SI_RECURRING 
		}), 
		@Variable(name = "instructionDate", type = Timestamp.class, format = Format.DATE_TIME, required = false), 
		@Variable(name = "sessionTime", required = false),
		@Variable(name = "recurringParamType", required = false, options = {
			ApplicationConstants.SI_RECURRING_TYPE_DAILY, 
			ApplicationConstants.SI_RECURRING_TYPE_WEEKLY, 
			ApplicationConstants.SI_RECURRING_TYPE_MONTHLY,
			ApplicationConstants.EMPTY_STRING
		}), 
		@Variable(name = "recurringParam", required = false, type = Integer.class, defaultValue = "0"),
		@Variable(name = "recurringStartDate", required = false, type = Timestamp.class), 
		@Variable(name = "recurringEndDate", required = false, type = Timestamp.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N, required = false),
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));
		
		return sweepInService.confirm(map);
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		vo = sweepInService.approve(vo);
		
		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		return sweepInService.reject(vo);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
			@SubVariable(name = "accountNo"),
			@SubVariable(name = "accountName"),
			@SubVariable(name = "accountTypeCode"),
			@SubVariable(name = "accountTypeName"),
			@SubVariable(name = "accountCurrencyCode"),
			@SubVariable(name = "accountCurrencyName"),
			@SubVariable(name = "accountBranchCode", required = false),
			@SubVariable(name = "accountBranchName", required = false)
		})			
	})	
	@Override
	public Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAccountGroupService.searchCorporateAccountGroupDetailForDebitOnlyGetMap((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = "sessionTime", type = List.class)
	})
	@Override
	public Map<String, Object> getTransactionSessionTime(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("sessionTime", sysParamService.getTransactionSessionTime());
		return resultMap;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		sweepInService.executeFutureTransactionScheduler(parameter);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		sweepInService.executeRecurringTransactionScheduler(parameter);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "executedResult", type = List.class, subVariables = {
			@SubVariable(name = "executedDate", format = Format.DATE_TIME, type = Timestamp.class),
			@SubVariable(name = "systemReferenceNo"),
			@SubVariable(name = "debitAccount"),
			@SubVariable(name = "debitAccountName"),
			@SubVariable(name = "debitAccountCurrency"),
			@SubVariable(name = "creditAccount", required = false),
			@SubVariable(name = "creditAccountName", required = false),
			@SubVariable(name = "creditAccountCurrency", required = false),
			@SubVariable(name = "transactionAmount", type = BigDecimal.class),
			@SubVariable(name = "transactionCurrency"),
			@SubVariable(name = "status", format = Format.I18N),
			@SubVariable(name = "errorCode", required = false),
			@SubVariable(name = "errorDscp", required = false, format = Format.I18N),
			@SubVariable(name = "senderRefNo", required = false),
			@SubVariable(name = "benRefNo", required = false),
			@SubVariable(name = "debitEquivalentAmount", required = false, type = BigDecimal.class),
			@SubVariable(name = "debitTransactionCurrency", required = false),
			@SubVariable(name = "debitExchangeRate", required = false, type = BigDecimal.class),
			@SubVariable(name = "creditTransactionCurrency", required = false),
			@SubVariable(name = "creditEquivalentAmount", required = false, type = BigDecimal.class),
			@SubVariable(name = "creditExchangeRate", required = false, type = BigDecimal.class),
			@SubVariable(name = "chargeAccount", required = false),
			@SubVariable(name = "chargeList", type = List.class, required = false, subVariables = {
				@SubSubVariable(name = "chargeType", required = false),
				@SubSubVariable(name = "chargeCurrency", required = false),
				@SubSubVariable(name = "chargeEquivalentAmount", required = false, type = BigDecimal.class),
				@SubSubVariable(name = "chargeExchangeRate", required = false, type = BigDecimal.class),
			})
		})
	})
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		return sweepInService.detailExecutedTransaction(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "cancelId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CANCEL }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		sweepInService.cancelTransaction(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100203");
		
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = ApplicationConstants.TRANS_CURRENCY)
	})
	@Override
	public Map<String, Object> getLocalCurrency(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.TRANS_CURRENCY, sysParamService.getLocalCurrency());
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { LiquidityConstants.SRVC_GPT_LIQ_SWEEP_IN }),
		@Variable(name = "subAccountList", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
			@SubVariable(name = "remainingAmount", type = BigDecimal.class, defaultValue = "0"),
			@SubVariable(name = "description", required = false)
		}),
		@Variable(name = "instructionMode", options = {
			ApplicationConstants.SI_IMMEDIATE, 
			ApplicationConstants.SI_FUTURE_DATE, 
			ApplicationConstants.SI_RECURRING 
		}), 
		@Variable(name = "instructionDate", type = Timestamp.class, format = Format.DATE_TIME, required = false), 
		@Variable(name = "sessionTime", required = false),
		@Variable(name = "recurringParamType", required = false, options = {
			ApplicationConstants.SI_RECURRING_TYPE_DAILY, 
			ApplicationConstants.SI_RECURRING_TYPE_WEEKLY, 
			ApplicationConstants.SI_RECURRING_TYPE_MONTHLY,
			ApplicationConstants.EMPTY_STRING
		}), 
		@Variable(name = "recurringParam", required = false, type = Integer.class, defaultValue = "0"),
		@Variable(name = "recurringStartDate", required = false, type = Timestamp.class), 
		@Variable(name = "recurringEndDate", required = false, type = Timestamp.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N, required = false),
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> validateSubAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));
		
		return sweepInService.confirm(map);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeSweepBackTransaction(String parameter) throws ApplicationException, BusinessException {
		sweepInService.executeSweepBackTransaction(parameter);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "cancelId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CANCEL }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException {
		sweepInService.cancelTransactionWF(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100203");
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "receiptId"),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return sweepInService.downloadTransactionStatus(map);
	}
}