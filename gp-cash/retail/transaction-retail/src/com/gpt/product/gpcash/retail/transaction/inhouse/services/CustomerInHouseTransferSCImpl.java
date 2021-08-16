package com.gpt.product.gpcash.retail.transaction.inhouse.services;

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
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubSubVariable;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.beneficiarylist.services.CustomerBeneficiaryListInHouseService;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;
import com.gpt.product.gpcash.retail.token.validation.services.CustomerTokenValidationService;
import com.gpt.product.gpcash.retail.transaction.globaltransaction.services.CustomerGlobalTransactionService;
import com.gpt.product.gpcash.retail.transaction.validation.services.CustomerTransactionValidationService;

@Validate
@Service
public class CustomerInHouseTransferSCImpl implements CustomerInHouseTransferSC {

	@Autowired
	private CustomerInHouseTransferService inhouseTransferService;
	
	@Autowired
	private CustomerTransactionValidationService transactionValidationService;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Autowired
	private CustomerUserPendingTaskService customerUserPendingTaskService;
	
	@Autowired
	private CustomerBeneficiaryListInHouseService beneficiaryListInHouseService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CustomerGlobalTransactionService globalTransactionService;
	
	@Autowired
	private CustomerTokenValidationService tokenValidationService;

	@SuppressWarnings("unchecked")
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_BEN_ID),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_NAME),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_CURRENCY),
		@Variable(name = "isBeneficiaryFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isSaveBenFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.TRANS_CURRENCY), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, type = BigDecimal.class), 
		@Variable(name = "chargeList", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value", type = BigDecimal.class),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = "totalCharge", type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { 
			ApplicationConstants.SRVC_GPT_FTR_IH_OWN, 
			ApplicationConstants.SRVC_GPT_FTR_IH_3RD
		}), 
		@Variable(name = "remark1", required = false), 
		@Variable(name = "remark2", required = false), 
		@Variable(name = "remark3", required = false),
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
		@Variable(name = "isNotify", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "notifyBenValue", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = ApplicationConstants.LOGIN_TOKEN_NO),
		@Variable(name = ApplicationConstants.CHALLENGE_NO),
		@Variable(name = ApplicationConstants.RESPONSE_NO)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.CUST_ID), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) 
				map.get(ApplicationConstants.CHALLENGE_NO), (String) 
				map.get(ApplicationConstants.RESPONSE_NO));
		
		transactionValidationService.validateChargeAndTotalTransaction((String) map.get(ApplicationConstants.CUST_ID), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT), (BigDecimal) map.get("totalCharge"), 
				(String)map.get(ApplicationConstants.APP_CODE), (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));
		
		transactionValidationService.validateLimit( 
				(String) map.get(ApplicationConstants.CUST_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"));
				
		Map<String, Object> resultMap = inhouseTransferService.submit(map);
		globalTransactionService.updateCreatedTransactionByUserCode(customerId);
		
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, inhouseTransferService);
		
		if(ApplicationConstants.NO.equals(vo.getIsError())) {
			resultMap = new HashMap<>();
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
			resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			resultMap.put("dateTime", strDateTime);
		} else {
			throw new BusinessException(vo.getErrorCode());
		}

		return resultMap;
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_BEN_ID),
		@Variable(name = "isBeneficiaryFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isSaveBenFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.TRANS_CURRENCY), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { ApplicationConstants.SRVC_GPT_FTR_IH_OWN, ApplicationConstants.SRVC_GPT_FTR_IH_3RD}), 
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
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
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
		
		transactionValidationService.validateLimit( 
				(String) map.get(ApplicationConstants.CUST_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"));

		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		String tokenNo = (String) map.get(ApplicationConstants.LOGIN_TOKEN_NO);
		
		if(!ValueUtils.hasValue(tokenNo)) {
			throw new BusinessException("GPT-0100153");
		}
		
		map = inhouseTransferService.confirm(map);
		
		map.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge(customerId, tokenNo));
		
		return map; 
	}

	@EnableCustomerActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		vo = inhouseTransferService.approve(vo);
		
		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@EnableCustomerActivityLog
	@Override
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		return inhouseTransferService.reject(vo);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_DTL_ID),
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
		return customerAccountService.findByCustomerIdAndIsDebit((String)map.get(ApplicationConstants.CUST_ID));
	}
	
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_DTL_ID),
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
	public Map<String, Object> searchOwnAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.findByCustomerIdAndIsCredit((String)map.get(ApplicationConstants.CUST_ID));
	}
	
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInHouseService.searchCustomerBeneficiary((String)map.get(ApplicationConstants.CUST_ID));
	}	
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
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
		inhouseTransferService.executeFutureTransactionScheduler(parameter);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		inhouseTransferService.executeRecurringTransactionScheduler(parameter);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), 
		@Variable(name = "benAccountName"), 
		@Variable(name = "benAccountCurrency") 
	})
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInHouseService.searchOnline(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "accountNo"), 
		@Variable(name = "accountName"),
		@Variable(name = "accountTypeCode"),
		@Variable(name = "accountTypeName"),		
		@Variable(name = "accountCurrencyCode"),
		@Variable(name = "accountCurrencyName"),
		@Variable(name = "availableBalance")
	})
	@Override
	public Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException {
		return globalTransactionService.checkBalance((String)map.get(ApplicationConstants.CUST_ID),
				(String) map.get(ApplicationConstants.ACCOUNT_DTL_ID));
	}
	
	@Validate
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
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
		return inhouseTransferService.detailExecutedTransaction(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "cancelId"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CANCEL }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		inhouseTransferService.cancelTransaction(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100203");
		
		return resultMap;
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "receiptId"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return inhouseTransferService.downloadTransactionStatus(map);
	}
}