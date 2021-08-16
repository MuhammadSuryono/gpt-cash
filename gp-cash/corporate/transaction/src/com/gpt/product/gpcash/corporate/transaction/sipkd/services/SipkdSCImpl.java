package com.gpt.product.gpcash.corporate.transaction.sipkd.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.domestic.services.DomesticTransferService;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class SipkdSCImpl implements SipkdSC {
	
	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private SipkdService SIPKDService;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private DomesticTransferService domesticTransferService;
	
	private final static String TRANS_SRVC = "transService";
	
	@SuppressWarnings("unchecked")
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.TRANS_BEN_ID),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_NAME),
		@Variable(name = ApplicationConstants.TRANS_BEN_ACCT_CURRENCY),
		@Variable(name = ApplicationConstants.TRANS_CURRENCY), 
		@Variable(name = ApplicationConstants.TRANS_AMOUNT, type = BigDecimal.class),
		@Variable(name = "chargeInstruction", options = { 
			ApplicationConstants.REMIITER_CHARGES_INSTRUCTION, 
			ApplicationConstants.BENEFICIARY_CHARGES_INSTRUCTION
		}),
		@Variable(name = "address1", required = false),
		@Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", required = false, options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", required = false, options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "benCitizenCountryCode", required = false),
		@Variable(name = "beneficiaryTypeCode", required = false),
		@Variable(name = "bankCode", required = false),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, type = BigDecimal.class), 
		@Variable(name = "chargeList", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value", type = BigDecimal.class),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = "totalCharge", type = BigDecimal.class),
		@Variable(name = "senderRefNo", required = false), 
		@Variable(name = "benRefNo", required = false), 
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { 
			ApplicationConstants.SRVC_GPT_FTR_SIPKD_IH_OWN,
			ApplicationConstants.SRVC_GPT_FTR_SIPKD_IH_3RD,
			ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_LLG, 
			ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_RTGS, 
			ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_ONLINE
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
		
		String transType = (String) map.get(TRANS_SRVC);
		if ("DOMESTIC".equals(this.isDomesticOrInhouse(transType))) {
			String sipkdSrvcCd = ((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE)).replace("SIPKD_", "");
			domesticTransferService.checkTransactionThreshold((BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), sipkdSrvcCd);
		
			transactionValidationService.validateHoliday((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
					(Timestamp) map.get("instructionDate"), 
					(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
					(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
					(String) map.get("sessionTime"),false, (String) map.get(ApplicationConstants.TRANS_CURRENCY));

			transactionValidationService.validateCOT((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
					(String) map.get(ApplicationConstants.TRANS_CURRENCY), (String)map.get(ApplicationConstants.APP_CODE),
					(String) map.get("sessionTime"), false, true);
		}
		
		transactionValidationService.validateChargeAndTotalTransaction((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT), (BigDecimal) map.get("totalCharge"), 
				(String)map.get(ApplicationConstants.APP_CODE), (ArrayList<Map<String,Object>>) map.get("chargeList"), null);
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), 
				(String) map.get("recurringParamType"), (Integer) map.get("recurringParam"),
				(Timestamp) map.get("recurringStartDate"), (Timestamp) map.get("recurringEndDate"),
				(String) map.get("sessionTime"));
		
		transactionValidationService.validateLimit((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"));
		
		Map<String, Object> resultMap = SIPKDService.submit(map);
		
		globalTransactionService.updateCreatedTransactionByUserCode(userCode);
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		String transType = vo.getTransactionServiceCode();
		if (transType.contains("LLG") || transType.contains("RTGS")) {
			
		transactionValidationService.validateHoliday(vo.getInstructionMode(), 
				vo.getInstructionDate(), 
				vo.getRecurringParamType(), vo.getRecurringParam() == null ?0 : vo.getRecurringParam(),
				vo.getRecurringStartDate(), vo.getRecurringEndDate(),
				vo.getSessionTime(),true, vo.getTransactionCurrency());
		
		transactionValidationService.validateCOT((String) vo.getInstructionMode(), 
				vo.getTransactionServiceCode(), 
				vo.getTransactionCurrency(), ApplicationConstants.APP_GPCASHIB,
				vo.getSessionTime(), true, true);
		}
		
		vo = SIPKDService.approve(vo);
		
		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		return SIPKDService.reject(vo);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "billId"), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = SIPKDService.confirm(map);
		
		resultMap.put("recurringParam", 1);
		resultMap.put("recurringEndDate", null);
		resultMap.put("sessionTime", "00:00");
		
		return resultMap;
	}

	@Override
	public Map<String, Object> searchSourceAccount(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return null;
	}

	@Override
	public Map<String, Object> searchOwnAccount(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> searchBeneficiary(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		return SIPKDService.detailExecutedTransaction(map);
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
		return SIPKDService.downloadTransactionStatus(map);
	}

	private String isDomesticOrInhouse(String srvcCd) {
		if("SKN".equals(srvcCd) 
				|| "RTGS".equals(srvcCd)
				|| "ONL".equals(srvcCd)) {
			return "DOMESTIC";
		}
		
		return "OVERBOOKING";
	}
}
