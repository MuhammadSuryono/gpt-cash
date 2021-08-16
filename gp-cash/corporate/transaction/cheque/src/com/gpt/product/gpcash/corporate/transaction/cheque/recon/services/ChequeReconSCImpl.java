package com.gpt.product.gpcash.corporate.transaction.cheque.recon.services;

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
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.services.ChequeOrderService;

@Validate
@Service
public class ChequeReconSCImpl implements ChequeReconSC {
	@Autowired
	private ChequeOrderService chequeOrderService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
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
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "chequeNo", required = false),
		@Variable(name = "orderNo", required = false),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.searchForCorporate(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name="chequeList", type = List.class, subVariables = {
		    	@SubVariable(name= "accountNo"),
		    	@SubVariable(name= "accountName"),
		    	@SubVariable(name= "accountCurrency"),
		    	@SubVariable(name= "noOfPages"),
		    	@SubVariable(name= "serialNo"),
		    })
	})	
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.searchOnlineForCorporate(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "serialNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
			@Variable(name= "chequeFrom"),
			@Variable(name= "chequeTo"),
			@Variable(name="chequeList", type = List.class, subVariables = {
			    	@SubVariable(name= "chequeNo"),
			    	@SubVariable(name= "status")
			    })
	})	
	@Override
	public Map<String, Object> findOnlineBySerialNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.findOnlineBySerialNo(map);
	}
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> findDetailByOrderNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.findDetailByOrderNo(map);
	}
}
