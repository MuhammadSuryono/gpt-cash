package com.gpt.product.gpcash.retail.inquiry.balance.services;

import java.util.ArrayList;
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
import com.gpt.component.maintenance.branch.services.BranchService;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;

@Validate
@Service
public class CustomerBalanceSummarySCImpl implements CustomerBalanceSummarySC {

	@Autowired
	private CustomerBalanceSummaryService balanceSummaryService;
	
	@Autowired
	private BranchService branchService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "casaInfo", type = Map.class),
		@Variable(name = "loanInfo", type = Map.class),
		@Variable(name = "tdInfo", type = Map.class)
	})	
	@Override
	public Map<String, Object> singleAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return balanceSummaryService.singleAccount(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "accountBranchCode", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = "casaInfo", type = Map.class),
		@Variable(name = "loanInfo", type = Map.class),
		@Variable(name = "tdInfo", type = Map.class)
	})	
	@Override
	public Map<String, Object> multiAccountByBranch(Map<String, Object> map) throws ApplicationException, BusinessException {
		return balanceSummaryService.multiAccountByBranch(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "accountTypeCode", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = "casaInfo", type = Map.class),
		@Variable(name = "loanInfo", type = Map.class),
		@Variable(name = "tdInfo", type = Map.class)
	})		
	@Override
	public Map<String, Object> multiAccountByAccountType(Map<String, Object> map) throws ApplicationException, BusinessException {
		return balanceSummaryService.multiAccountByAccountType(map);
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
		List<String> casaAccountType = new ArrayList<>();
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_LOAN);
	    
		return customerAccountService.findCASAAccountByCustomerAndAccountTypeForInquiryOnlyGetMap((String) map.get(ApplicationConstants.CUST_ID), casaAccountType);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchAccountType(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_ACCT_TYP");
		return parameterMaintenanceService.searchModel(map);
	}
}
