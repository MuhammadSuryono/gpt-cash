package com.gpt.product.gpcash.corporate.transactionstatus.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SortingItem;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;

@Validate
@Service
public class TransactionStatusAdminSCImpl implements TransactionStatusAdminSC {
	@Autowired
	private TransactionStatusAdminService transactionStatusService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
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
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "menus", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskCode"),
			@SubVariable(name = "pendingTaskName"),
		})			
	})	
	@Override
	public Map<String, Object> searchUserGroupMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.findMenuForPendingTask((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
		@SortingItem("referenceNo"),
		@SortingItem("activityDate"),
		@SortingItem("pendingTaskMenuCode"),
		@SortingItem("sourceAccount"),
		@SortingItem("transactionAmount"),
		@SortingItem("status"),
	})
	@Input({
		@Variable(name = "senderRefNo", required = false),
		@Variable(name = "benRefNo", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = "creationDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "creationDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "corpAccount", required = false),
		@Variable(name = "pendingTaskMenuCode", required = false),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskId"),
			@SubVariable(name = "referenceNo"),
			@SubVariable(name = "pendingTaskMenuCode"),
			@SubVariable(name = "pendingTaskMenuName"),
			@SubVariable(name = "sourceAccount", required = false),
			@SubVariable(name = "sourceAccountName", required = false),
			@SubVariable(name = "sourceAccountCurrencyCode", required = false),
			@SubVariable(name = "sourceAccountCurrencyName", required = false),
			@SubVariable(name = "transactionAmount", required = false, type = BigDecimal.class),
			@SubVariable(name = "transactionCurrency", required = false),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})	
	@Override
	public Map<String, Object> searchTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.searchTransactionStatus(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "pendingTaskId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "activities", type = List.class, subVariables = {
			@SubVariable(name = "activityDate"),
			@SubVariable(name = "activity", format = Format.I18N),
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName"),
			@SubVariable(name = "amountCcyCd", required = false),
			@SubVariable(name = "amount", required = false, type = BigDecimal.class),
			@SubVariable(name = "approvalLvCount", required = false),
			@SubVariable(name = "approvalLvRequired", required = false),
			@SubVariable(name = "status", format = Format.I18N),
			@SubVariable(name = "isExecute", required = false),
			@SubVariable(name = "isCreate", required = false),
		})			
	})	
	@Override
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.detailTransactionStatus(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = "executedMenuCode"),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.detailExecutedTransaction(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = pendingTaskService.detailPendingTask(map);
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "senderRefNo", required = false),
		@Variable(name = "benRefNo", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = "creationDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "creationDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "corpAccount", required = false),
		@Variable(name = "pendingTaskMenuCode", required = false),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionStatusService.downloadActivity(map);
	}
	
}
