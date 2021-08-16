package com.gpt.product.gpcash.corporate.transaction.payroll.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubSubVariable;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.services.PendingUploadService;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class PayrollSCImpl implements PayrollSC {
	
	@Autowired
	private PayrollService payrollService;
	
	@Autowired
	private PendingUploadService pendingUploadService;
	
	@Autowired
	private TransactionValidationService transactionValidationService;	
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;	
	
	@Autowired
	private SysParamService sysParamService;	
	
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
	@Output({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME)
	})
	@Override
	public Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException {
		return payrollService.upload(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "fileFormat", options = {
			ApplicationConstants.FILE_FORMAT_CSV,
			ApplicationConstants.FILE_FORMAT_TXT
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {PayrollSC.menuCode} )
	})	
	@Override
	public Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException {
		return payrollService.downloadSample(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DETAIL}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = PayrollSC.menuCode )
	})	
	@Override
	public Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException {		
		return pendingUploadService.searchPendingUploadById(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { PendingUploadConstants.SRVC_GPT_MFT_PROLL_IH }), 		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = PayrollSC.menuCode )
	})	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> pendingUploadInfo = pendingUploadService.searchPendingUploadByIdValidOnly(map);				
		map.putAll(pendingUploadInfo);
		
		transactionValidationService.validateMaximumRecordBulkTransaction(((Long)map.get("totalRecord")).intValue());
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE), null, 0, null, null, 
				(String)map.get(ApplicationConstants.SESSION_TIME));
		
		String immediateType = sysParamService.getValueByCode(SysParamConstants.IMMEDIATE_TYPE);
		if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_CREATE)){
			transactionValidationService.validateCOT((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
					(String) map.get(ApplicationConstants.TRANS_CURRENCY), (String)map.get(ApplicationConstants.APP_CODE),
					(String) map.get("sessionTime"), false, true);
		}
		
		transactionValidationService.validateLimit((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"));

		return payrollService.confirm(map);
	}	
	
	@SuppressWarnings("unchecked")
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.TRANS_CHARGE_LIST, type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value", type = BigDecimal.class),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_CHARGE, type = BigDecimal.class),
		@Variable(name = ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, type = BigDecimal.class), 		
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, options = { PendingUploadConstants.SRVC_GPT_MFT_PROLL_IH }), 		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = PayrollSC.menuCode )
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> pendingUploadInfo = pendingUploadService.searchPendingUploadByIdValidOnly(map);		
		map.putAll(pendingUploadInfo);
		
		transactionValidationService.validateMaximumRecordBulkTransaction(((Long)map.get("totalRecord")).intValue());
		
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE), null, 0, null, null, 
				(String)map.get(ApplicationConstants.SESSION_TIME));

		transactionValidationService.validateChargeAndTotalTransactionPerRecords((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				(ArrayList<Map<String,Object>>) map.get(ApplicationConstants.TRANS_CHARGE_LIST),
				(Long)map.get("totalRecord"));
		
		String immediateType = sysParamService.getValueByCode(SysParamConstants.IMMEDIATE_TYPE);
		if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_CREATE)){
			transactionValidationService.validateCOT((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
					(String) map.get(ApplicationConstants.TRANS_CURRENCY), (String)map.get(ApplicationConstants.APP_CODE),
					(String) map.get("sessionTime"), false, true);
		}
		
		transactionValidationService.validateLimit((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE), 
				(String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
				(BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT), 
				(String) map.get(ApplicationConstants.TRANS_CURRENCY), 
				(String)map.get(ApplicationConstants.APP_CODE), 
				map.get("instructionDate"),
				(String)map.get("instructionMode"),
				map.get("recurringStartDate"));	
		
		return payrollService.submit(map);
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		transactionValidationService.validateCOT((String) vo.getInstructionMode(), 
				vo.getTransactionServiceCode(), 
				vo.getTransactionCurrency(), ApplicationConstants.APP_GPCASHIB,
				vo.getSessionTime(), true, true);
		
		vo = payrollService.approve(vo);
		
		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		return null;
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
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = "fileFormats", type = List.class)
	})
	@Override
	public Map<String, Object> getFileFormats(Map<String, Object> map) throws ApplicationException, BusinessException {
		List<String> supportedFileFormat = new ArrayList<>();
		supportedFileFormat.add(ApplicationConstants.FILE_FORMAT_CSV);
		supportedFileFormat.add(ApplicationConstants.FILE_FORMAT_TXT);		
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("fileFormats", supportedFileFormat);
		
		return resultMap;
	}	

	@Override
	public void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		payrollService.executeImmediateTransactionScheduler(parameter);
	}

	@Override
	public void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		payrollService.executeFutureTransactionScheduler(parameter);
	}

	@Override
	public void executePayrollResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		payrollService.executePayrollResponseScheduler(parameter);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "executedResult", type = List.class, subVariables = {
			@SubVariable(name = "executedDate", format = Format.DATE_TIME, type = Date.class),
			@SubVariable(name = "systemReferenceNo"),
			@SubVariable(name = "debitAccount"),
			@SubVariable(name = "debitAccountName"),
			@SubVariable(name = "debitAccountCurrency", required = false),
			@SubVariable(name = "creditAccount"),
			@SubVariable(name = "creditAccountName"),			
			@SubVariable(name = "transactionCurrency", required = false),
			@SubVariable(name = "transactionAmount"),
			@SubVariable(name = "status", format = Format.I18N),
			@SubVariable(name = "errorCode", required = false),
			@SubVariable(name = "debitEquivalentAmount", required = false),
			@SubVariable(name = "debitTransactionCurrency", required = false),
			@SubVariable(name = "debitExchangeRate", required = false),
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
		return payrollService.detailExecutedTransaction(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "details", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
			@SubVariable(name = "benAccountName"),
			@SubVariable(name = "trxCurrencyCode", required = false),
			@SubVariable(name = "trxAmount"),
			@SubVariable(name = "description", required = false),
			@SubVariable(name = "benRefNo", required = false),
			@SubVariable(name = "finalizeFlag"),
			@SubVariable(name = "senderRefNo", required = false)
		})
	})	
	@Override
	public Map<String, Object> detailCreatedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.detailCreatedTransaction(map);
	}	
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DETAIL }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "details", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
			@SubVariable(name = "benAccountName"),
			@SubVariable(name = "trxCurrencyCode", required = false),
			@SubVariable(name = "trxAmount"),
			@SubVariable(name = "description", required = false),
			@SubVariable(name = "benRefNo", required = false),
			@SubVariable(name = "finalizeFlag"),
			@SubVariable(name = "senderRefNo", required = false)
		})
	})	
	@Override
	public Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.searchPendingUploadDetail(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "pendingUploadId", type = List.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = PayrollSC.menuCode )
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})	
	@Override
	public Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException {		
		return pendingUploadService.deletePendingUpload(map);
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
		payrollService.cancelTransaction(map);
		
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
		return payrollService.downloadTransactionStatus(map);
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
		payrollService.cancelTransactionWF(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100203");
		
		return resultMap;
	}
	
	@Override
	public void executePayrollVAResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		payrollService.executePayrollVAResponseScheduler(parameter);
	}
	
	@Override
	public void executePayrollUpdateHeaderScheduler(String parameter) throws ApplicationException, BusinessException {
		payrollService.executePayrollUpdateHeaderScheduler(parameter);
	}
}