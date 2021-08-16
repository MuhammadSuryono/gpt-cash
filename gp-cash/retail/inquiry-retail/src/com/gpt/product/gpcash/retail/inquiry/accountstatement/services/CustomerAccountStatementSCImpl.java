package com.gpt.product.gpcash.retail.inquiry.accountstatement.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;

@Validate
@Service
public class CustomerAccountStatementSCImpl implements CustomerAccountStatementSC {
	
	@Autowired
	private CustomerAccountStatementService accountStatementService;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "periods", type = List.class, subVariables = {
			@SubVariable(name = "month", type = Integer.class),
			@SubVariable(name = "year", type = Integer.class)
		})
	})		
	@Override
	public Map<String, Object> getPeriods(Map<String, Object> map) throws ApplicationException, BusinessException {
		return accountStatementService.getPeriods(map);
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
	public Map<String, Object> searchCustomerAccountForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.findCASAAccountByCustomerAndAccountTypeForInquiryOnlyGetMap((String) map.get(ApplicationConstants.CUST_ID), null);
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = "month", type = Integer.class),
		@Variable(name = "year", type = Integer.class),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "totalDebit", type = BigDecimal.class),
		@Variable(name = "totalCredit", type = BigDecimal.class),
		@Variable(name = "transactions", type = List.class, subVariables = {
			@SubVariable(name = "postDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "effectiveDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "debitAmount", type = BigDecimal.class),
			@SubVariable(name = "creditAmount", type = BigDecimal.class),
			@SubVariable(name = "balance", required = false, type = BigDecimal.class),
			@SubVariable(name = "description", required = false),
			@SubVariable(name = "referenceNo", required = false)
		})
	})		
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return accountStatementService.search(map);
	}		
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = "month", type = Integer.class),
		@Variable(name = "year", type = Integer.class),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> download(Map<String, Object> map) throws ApplicationException, BusinessException {
		return accountStatementService.download(map);
	}	

}
