package com.gpt.product.gpcash.corporate.transaction.international.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.gpt.component.maintenance.branch.services.BranchService;
import com.gpt.component.maintenance.internationalbank.services.InternationalBankService;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.beneficiarylist.services.BeneficiaryListInternationalService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.token.validation.services.TokenValidationService;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class InternationalTransferSCImpl implements InternationalTransferSC {

	@Autowired
	private InternationalTransferService internationalTransferService;
	
	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
		
	@Autowired
	private BeneficiaryListInternationalService beneficiaryListInternationalService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@Autowired
	private InternationalBankService internationalBankService;
	
	@Autowired
	private BranchService branchService;
	
	@Autowired
	private TokenValidationService tokenValidationService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateWFEngine wfEngine;

	@SuppressWarnings("unchecked")
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_BEN_ID),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_NAME),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_CURRENCY),
		@Variable(name = "isBeneficiaryFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isSaveBenFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.TRANS_CURRENCY), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = "chargeInstruction", options = { 
			ApplicationConstants.REMIITER_CHARGES_INSTRUCTION, 
			ApplicationConstants.BENEFICIARY_CHARGES_INSTRUCTION
		}),
		@Variable(name = "benAliasName", required = false),
		@Variable(name = "address1", required = false),
		@Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", required = false, options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", required = false, options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "benCitizenCountryCode", required = false),
		@Variable(name = "beneficiaryTypeCode", required = false),
		@Variable(name = "bankCode"),
		@Variable(name = "branchCode"),
		@Variable(name = "underlyingCode", required = false),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, type = BigDecimal.class), 
		@Variable(name = "chargeList", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value", type = BigDecimal.class),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = "totalCharge", type = BigDecimal.class),
		@Variable(name = "senderRefNo", required = false), 
		@Variable(name = "isFinalPayment", options = {ApplicationConstants.YES, ApplicationConstants.NO}), 
		@Variable(name = "benRefNo", required = false), 
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { 
			ApplicationConstants.SRVC_GPT_INT
		}), 
		@Variable(name = "remark1", required = false), 
		@Variable(name = "remark2", required = false), 
		@Variable(name = "remark3", required = false),
		@Variable(name = "exchangeRate", options = { 
				ApplicationConstants.RATE_COUNTER, 
				ApplicationConstants.RATE_SPECIAL,
				ApplicationConstants.RATE_FORWARD_CONTRACT
			}, required = false, defaultValue = ApplicationConstants.RATE_COUNTER),
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
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		/*transactionValidationService.validateChargeAndTotalTransaction((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT), (BigDecimal) map.get("totalCharge"), 
				(String)map.get(ApplicationConstants.APP_CODE), (ArrayList<Map<String,Object>>) map.get("chargeList"));*/
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));

		transactionValidationService.validateHoliday((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"),false, (String) map.get(ApplicationConstants.TRANS_CURRENCY));

		transactionValidationService.validateCOT((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), (String)map.get(ApplicationConstants.APP_CODE),
				(String) map.get("sessionTime"), false, true);
		
		transactionValidationService.validateLimitEquivalent((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"),
				ApplicationConstants.RATE_COUNTER,"");
		
		Map<String, Object> resultMap = internationalTransferService.submit(map);
		
		globalTransactionService.updateCreatedTransactionByUserCode(userCode);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError())) {
				throw new BusinessException(vo.getErrorCode());				
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
			globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		}
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = ApplicationConstants.TRANS_BEN_ID),
		@Variable(name = "isBeneficiaryFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isSaveBenFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isFinalPayment", options = {ApplicationConstants.YES, ApplicationConstants.NO}), 
		@Variable(name = "senderRefNo", required = false),
                @Variable(name = ApplicationConstants.TRANS_BEN_ACCT_NAME),	
		@Variable(name = ApplicationConstants.TRANS_CURRENCY), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { ApplicationConstants.SRVC_GPT_INT}), 
		@Variable(name = "bankCode"),
		@Variable(name = "branchCode"),
		@Variable(name = "underlyingCode", required = false),
		@Variable(name = "exchangeRate", options = { 
				ApplicationConstants.RATE_COUNTER, 
				ApplicationConstants.RATE_SPECIAL,
				ApplicationConstants.RATE_FORWARD_CONTRACT
			}, required = false, defaultValue = ApplicationConstants.RATE_COUNTER),
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
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));

		transactionValidationService.validateHoliday((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"),false, (String) map.get(ApplicationConstants.TRANS_CURRENCY));

		transactionValidationService.validateCOT((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), (String)map.get(ApplicationConstants.APP_CODE),
				(String) map.get("sessionTime"), false, true);
		
		String treasuryCode = map.get("treasuryCode")!=null?(String)map.get("treasuryCode"):"";
		
		Map<String, Object> limitMap = transactionValidationService.validateLimitEquivalent((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"),
				(String)map.get("exchangeRate"),
				treasuryCode);
		
		map.putAll(limitMap);
		
		String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String tokenNo = (String) map.get(ApplicationConstants.LOGIN_TOKEN_NO);
		
		map = internationalTransferService.confirm(map);

		map.put(ApplicationConstants.IS_ONE_SIGNER, isOneSigner);
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
			if(!ValueUtils.hasValue(tokenNo)) {
				throw new BusinessException("GPT-0100153");
			}
			map.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge(corpId,userCode,tokenNo));				
		}
		
		return map; 
		
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		transactionValidationService.validateHoliday(vo.getInstructionMode(), 
				vo.getInstructionDate(), 
				vo.getRecurringParamType(), vo.getRecurringParam() == null ?0 : vo.getRecurringParam(),
				vo.getRecurringStartDate(), vo.getRecurringEndDate(),
				vo.getSessionTime(),true, vo.getTransactionCurrency());
		
		transactionValidationService.validateCOT((String) vo.getInstructionMode(), 
				vo.getTransactionServiceCode(), 
				vo.getTransactionCurrency(), ApplicationConstants.APP_GPCASHIB,
				vo.getSessionTime(), true, true);
		
		vo = internationalTransferService.approve(vo);
		
		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		return internationalTransferService.reject(vo);
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
		return corporateAccountGroupService.searchCorporateAccountGroupDetailForDebitOnlyMultiCurrencyGetMap((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInternationalService.searchBeneficiary((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
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
		internationalTransferService.executeFutureTransactionScheduler(parameter);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		internationalTransferService.executeRecurringTransactionScheduler(parameter);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		return globalTransactionService.checkBalance((String)map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.LOGIN_USERCODE),
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_BENEFICIARY_TYP"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	
	@Validate
	@Input({
		@Variable(name = "countryCode", required = true),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL)
	})
	@Override
	public Map<String, Object> searchBankByCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalBankService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_COUNTRY"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchTrxPurposeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_TRX_PURPOSE"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	@Override
	public Map<String, Object> searchCurrencyForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_CURRENCY"); 
		return parameterMaintenanceService.searchModel(map);
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
		return internationalTransferService.detailExecutedTransaction(map);
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
		internationalTransferService.cancelTransaction(map);
		
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
		return internationalTransferService.downloadTransactionStatus(map);
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBranchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.search(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = "treasuryCode", required = false),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = ApplicationConstants.TRANS_CURRENCY),
		@Variable(name = "sourceAccountCurrency"), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = "exchangeRate", options = { 
			ApplicationConstants.RATE_COUNTER, 
			ApplicationConstants.RATE_SPECIAL,
			ApplicationConstants.RATE_FORWARD_CONTRACT
		}),
		@Variable(name = "instructionMode", options = { 
				ApplicationConstants.SI_IMMEDIATE, 
				ApplicationConstants.SI_FUTURE_DATE, 
				ApplicationConstants.SI_RECURRING 
			}),
		@Variable(name = "currencyMatrix", options = { 
				ApplicationConstants.CCY_MTRX_FL
			}),
		@Variable(name = "instructionDate", type = Timestamp.class, format = Format.DATE_TIME, required = false),
	})
	@Override
	public Map<String, Object> checkRate(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		if (String.valueOf(map.get("exchangeRate")).equals(ApplicationConstants.RATE_COUNTER)) {
			return transactionValidationService.validateCounterRate((String) map.get("sourceAccountCurrency"), (String) map.get(ApplicationConstants.TRANS_CURRENCY),new BigDecimal((map.get(ApplicationConstants.TRANS_AMOUNT).toString())));
		} else if (String.valueOf(map.get("exchangeRate")).equals(ApplicationConstants.RATE_SPECIAL))  {
			String treasuryCode = (String) map.get("treasuryCode");
			String sourceAccountCurrency = (String) map.get("sourceAccountCurrency");
			String transactionCurrency = (String) map.get(ApplicationConstants.TRANS_CURRENCY);
			BigDecimal trxAmount = new BigDecimal((map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
			Timestamp instructionDate = (Timestamp) map.get("instructionDate");
			String instructionMode = (String) map.get("instructionMode");
			String corporateID = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			return transactionValidationService.checkSpecialRate(treasuryCode,sourceAccountCurrency, transactionCurrency,trxAmount,instructionDate,corporateID,instructionMode);
		}else {
			String treasuryCode = (String) map.get("treasuryCode");
			String sourceAccountCurrency = (String) map.get("sourceAccountCurrency");
			String transactionCurrency = (String) map.get(ApplicationConstants.TRANS_CURRENCY);
			BigDecimal trxAmount = new BigDecimal((map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
			Timestamp instructionDate = (Timestamp) map.get("instructionDate");
			String instructionMode = (String) map.get("instructionMode");
			String corporateID = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			return transactionValidationService.checkForwardContract(treasuryCode,sourceAccountCurrency, transactionCurrency,trxAmount,instructionDate,corporateID,instructionMode);
		}
	}
}