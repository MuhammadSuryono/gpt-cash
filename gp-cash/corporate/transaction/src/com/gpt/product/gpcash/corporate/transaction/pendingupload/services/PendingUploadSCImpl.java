package com.gpt.product.gpcash.corporate.transaction.pendingupload.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class PendingUploadSCImpl implements PendingUploadSC {
	
	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private PendingUploadService pendingUploadService;
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {
				PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_PAYROLL, 
				PendingUploadConstants.MNU_GPCASH_F_DIRECT_DEBIT,
				PendingUploadConstants.MNU_GPCASH_F_CORP_VA_UPLOAD,
				PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_BULK_PAYMENT
		}) 
	})
	@Output({
		@Variable(name = "result", type = List.class, required = false, subVariables= {
			@SubVariable(name = "id"),	
			@SubVariable(name = "status"),
			@SubVariable(name = "statusDescription"),
			@SubVariable(name = ApplicationConstants.FILENAME),
			@SubVariable(name = "fileDescription", required = false),
			@SubVariable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME),
			@SubVariable(name = "totalRecord", type = Integer.class),
			@SubVariable(name = "totalError", type = Integer.class)				
		})
	})	
	@Override
	public Map<String, Object> searchPendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.searchPendingUpload(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.INSTRUCTION_MODE, options = {
			ApplicationConstants.SI_IMMEDIATE, 
			ApplicationConstants.SI_FUTURE_DATE
		}),
		@Variable(name = ApplicationConstants.INSTRUCTION_DATE, type = Timestamp.class, format = Format.DATE_TIME, required = false),
		@Variable(name = ApplicationConstants.SESSION_TIME),
		@Variable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME),
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "fileFormat", options = {
			ApplicationConstants.FILE_FORMAT_CSV,
			ApplicationConstants.FILE_FORMAT_TXT
		}),
		@Variable(name = "fileDescription", required = false),
		@Variable(name = ApplicationConstants.EAI_SERVICE_NAME),
		@Variable(name = ApplicationConstants.PATH_UPLOAD),		
		@Variable(name = ApplicationConstants.TRANS_SERVICE_CODE, required = false, options = { 
				PendingUploadConstants.SRVC_GPT_MFT_PROLL_IH, 
				PendingUploadConstants.SRVC_GPT_MFT_DIRECT_DEBIT,
				PendingUploadConstants.SRVC_GPT_MFT_BULK_IH,
				PendingUploadConstants.SRVC_GPT_MFT_BULK_DOM_LLG,
				PendingUploadConstants.SRVC_GPT_MFT_BULK_DOM_RTGS,
				PendingUploadConstants.SRVC_GPT_MFT_BULK_DOM_ONLINE
		}), 		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE_BUCKET}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {
				PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_PAYROLL, 
				PendingUploadConstants.MNU_GPCASH_F_DIRECT_DEBIT,
				PendingUploadConstants.MNU_GPCASH_F_CORP_VA_UPLOAD,
				PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_BULK_PAYMENT
		}) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitBucket(Map<String, Object> map) throws ApplicationException, BusinessException {
		transactionValidationService.validateInstructionMode((String) map.get(ApplicationConstants.INSTRUCTION_MODE), 
				(Timestamp) map.get("instructionDate"), null, 0, null, null,
				(String) map.get("sessionTime"));
				
		return pendingUploadService.submitToBucket(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "beneficiary", options = {
				ApplicationConstants.BENELIST_TYPE_INTERNAL_BANK,
				ApplicationConstants.BENELIST_TYPE_LOCAL_BANK,
				ApplicationConstants.BENELIST_TYPE_LOCAL_ONLINE_BANK,
				ApplicationConstants.BENELIST_TYPE_OVERSEAS_BANK,
				ApplicationConstants.BENELIST_TYPE_VIRTUAL_ACCOUNT
		}),
		@Variable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME),
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "fileFormat", options = {
			ApplicationConstants.FILE_FORMAT_CSV,
			ApplicationConstants.FILE_FORMAT_TXT
		}),
		@Variable(name = "fileDescription", required = false),
		@Variable(name = ApplicationConstants.EAI_SERVICE_NAME),
		@Variable(name = ApplicationConstants.PATH_UPLOAD),				
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE_BUCKET}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {PendingUploadConstants.MNU_GPCASH_F_FUND_BENEFICIARY_UPLOAD}) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitBucketBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.submitBeneficiaryToBucket(map);
	}
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {
				PendingUploadConstants.MNU_GPCASH_F_FUND_BENEFICIARY_UPLOAD
		}) 
	})
	@Output({
		@Variable(name = "result", type = List.class, required = false, subVariables= {
			@SubVariable(name = "id"),	
			@SubVariable(name = "status"),
			@SubVariable(name = "statusDescription"),
			@SubVariable(name = ApplicationConstants.FILENAME),
			@SubVariable(name = "fileDescription", required = false),
			@SubVariable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME),
			@SubVariable(name = "totalRecord", type = Integer.class),
			@SubVariable(name = "totalError", type = Integer.class)				
		})
	})	
	@Override
	public Map<String, Object> searchPendingUploadBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.searchPendingUploadBeneficiary(map);
	}
}
