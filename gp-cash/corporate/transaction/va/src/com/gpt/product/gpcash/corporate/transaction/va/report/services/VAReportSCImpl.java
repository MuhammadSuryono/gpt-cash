package com.gpt.product.gpcash.corporate.transaction.va.report.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.transaction.va.registration.services.VARegistrationService;

@Validate
@Service
public class VAReportSCImpl implements VAReportSC {
	
	@Autowired
	private VAReportService vaReportService;
	
	@Autowired
	private VARegistrationService vaRegistrationService;
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
			@Variable(name = "mainAccountNo"),
			@Variable(name = "productCode"),
			@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
			@Variable(name = "toDate", type = Date.class, format = Format.DATE),
			@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = "mainAccountNo"),
		@Variable(name = "mainAccountName"),
		@Variable(name = "mainAccountCurrencyCode"),
		@Variable(name = "corpProductCode"),
		@Variable(name = "corporateId"),
		@Variable(name = "corporateName"),
		@Variable(name = "vaTrxList", type = List.class, subVariables = { 
				@SubVariable(name = "postingDate", type = Date.class, format = Format.DATE),
				@SubVariable(name = "vaNo"), 
				@SubVariable(name = "vaName"),
				@SubVariable(name = "dscp", required = false),
				@SubVariable(name = "amount", type = BigDecimal.class)
		})
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaReportService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = "accountNo"),
			@SubVariable(name = "accountName"),
			@SubVariable(name = "accountCurrencyCode"),
			@SubVariable(name = "accountCurrencyName")
		})			
	})	
	@Override
	public Map<String, Object> searchMainAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.searchMainAccountByCorporate(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchProductCode(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaReportService.searchProductForDroplist(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "mainAccountNo"),
		@Variable(name = "productCode"),
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadVAReportPDF(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		return vaReportService.downloadVAReportPDF(map);
	}
}
