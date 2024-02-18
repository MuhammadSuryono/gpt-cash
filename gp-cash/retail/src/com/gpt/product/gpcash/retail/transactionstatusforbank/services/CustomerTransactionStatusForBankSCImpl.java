package com.gpt.product.gpcash.retail.transactionstatusforbank.services;

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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.customer.services.CustomerService;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.transactionstatus.services.CustomerTransactionStatusService;

@Validate
@Service
public class CustomerTransactionStatusForBankSCImpl implements CustomerTransactionStatusForBankSC {
	@Autowired
	private CustomerTransactionStatusService transactionStatusService;
	
	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Autowired
	private CustomerService customerService;
	
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
		return customerAccountService.findByCustomerIdAndIsDebit((String) map.get(ApplicationConstants.CUST_ID));
	}
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
		@SortingItem("referenceNo"),
		@SortingItem("activityDate"),
		@SortingItem("pendingTaskMenuCode"),
		@SortingItem("sourceAccount"),
		@SortingItem("transactionAmount"),
		@SortingItem("status"),
	})
	@Input({
		@Variable(name = "status", required = false),
		@Variable(name = "creationDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "creationDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "custAccount", required = false),
		@Variable(name = "pendingTaskMenuCode", required = false),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
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
	public Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionStatusService.searchTransactionStatus(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "pendingTaskId"),
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
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionStatusService.detailTransactionStatus(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = "executedMenuCode"),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
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

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
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
	public Map<String, Object> searchUserMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.findMenuForPendingTask((String)map.get(ApplicationConstants.CUST_ID));
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
			@SortingItem("referenceNo"),
			@SortingItem("activityDate"),
			@SortingItem("pendingTaskMenuCode"),
			@SortingItem("sourceAccount"),
			@SortingItem("transactionAmount"),
			@SortingItem("status"),
	})
	@Input({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
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
	public Map<String, Object> searchTransactionStatusByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionStatusService.searchTransactionStatusForBank(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.CUST_ID),
			@SubVariable(name = "customerName"),
		})
	})	
	@Override
	public Map<String, Object> searchUserMaker(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		return customerService.searchCustomers();
	}
}
