package com.gpt.product.gpcash.corporate.corporateaccount.services;

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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class CorporateAccountSCImpl implements CorporateAccountSC {

	@Autowired
	private CorporateAccountService corporateAccountService;
	
	@Autowired
	private CorporateService corporateService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, required = false, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.search(map);
	}
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAccountService.search(map);
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
		return corporateAccountService.searchOnlineByCIF(map);
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
		return corporateAccountService.searchOnlineByAccountNo(map);
	}	
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
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
		return corporateAccountService.submit(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
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
		return corporateAccountService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateAccountService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateAccountService.reject(vo);
	}

}
