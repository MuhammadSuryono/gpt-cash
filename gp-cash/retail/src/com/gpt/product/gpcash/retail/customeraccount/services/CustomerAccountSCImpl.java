package com.gpt.product.gpcash.retail.customeraccount.services;

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
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class CustomerAccountSCImpl implements CustomerAccountSC {

	@Autowired
	private CustomerAccountService customerAccountService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = @SortingItem(name = "customer.hostCifId", alias = "cifId"))
	@Input({
		@Variable(name = ApplicationConstants.USER_ID, required = false, format = Format.UPPER_CASE),
		@Variable(name = "accountNo", required = false),
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.search(map);
	}
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchByCustomerId(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.searchByCustomerId(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cifId"), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "cifId"),
		@Variable(name = "accountList", type = List.class, subVariables = {
			@SubVariable(name = "accountNo"),
			@SubVariable(name = "accountName"),
			@SubVariable(name = "accountTypeCode"),
			@SubVariable(name = "accountTypeName"),
			@SubVariable(name = "accountCurrencyCode"),
			@SubVariable(name = "accountCurrencyName"),
			@SubVariable(name = "accountBranchCode", required = false),
			@SubVariable(name = "accountBranchName", required = false),
			@SubVariable(name = "accountStatus", required = false),
			@SubVariable(name = "isAllowDebit"),
			@SubVariable(name = "isAllowCredit"),
			@SubVariable(name = "isAllowInquiry")
		}),
	})	
	@Override
	public Map<String, Object> searchOnlineByCIF(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.searchOnlineByCIF(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "accountNo"), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "cifId", required = false),
		@Variable(name = "accountNo"),
		@Variable(name = "accountName"),
		@Variable(name = "accountTypeCode"),
		@Variable(name = "accountTypeName"),
		@Variable(name = "accountCurrencyCode"),
		@Variable(name = "accountCurrencyName"),
		@Variable(name = "accountBranchCode", required = false),
		@Variable(name = "accountBranchName", required = false),
		@Variable(name = "accountStatus", required = false),
		@Variable(name = "isAllowDebit"),
		@Variable(name = "isAllowCredit"),
		@Variable(name = "isAllowInquiry")
	})
	@Override
	public Map<String, Object> searchOnlineByAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.searchOnlineByAccountNo(map);
	}	
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "accountList", type = List.class, subVariables = {
			@SubVariable(name = "accountNo"),
			@SubVariable(name = "accountCurrencyCode"),
			@SubVariable(name = "isAllowDebit", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
			@SubVariable(name = "isAllowCredit", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
			@SubVariable(name = "isAllowInquiry", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
			@SubVariable(name = "isInactiveFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
			@SubVariable(name = "accountTypeCode"),
			@SubVariable(name = "accountBranchCode", required = false),
			@SubVariable(name = "cifId")}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_CREATE,
			ApplicationConstants.WF_ACTION_UPDATE 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.submit(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = "accountList", type = List.class, subVariables = {
			@SubVariable(name = "accountNo")
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DELETE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerAccountService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return customerAccountService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return customerAccountService.reject(vo);
	}

}
