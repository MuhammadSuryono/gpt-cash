package com.gpt.product.gpcash.corporate.transaction.va.registration.services;

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
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class VARegistrationSCImpl implements VARegistrationSC {
	
	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private VARegistrationService vaRegistrationService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = @SortingItem(name = "id", alias = ApplicationConstants.CORP_ID))
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
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DETAIL}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchVAByCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.searchVAListByCorporate(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchCorporateAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.searchCorporateAccount((String) map.get(ApplicationConstants.CORP_ID));
	}
	
	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = "vaStatus"),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE_STATUS}),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.submit(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = "accountList", type = List.class, subVariables = { 
					@SubVariable(name = "mainAccountNo"),
					@SubVariable(name = "productCode"), 
					@SubVariable(name = "productName"),
					@SubVariable(name = "idx", type = Integer.class)}),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE}),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submitAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.submit(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = "accountList", type = List.class, subVariables = { @SubVariable(name = "id") }),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DELETE),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return vaRegistrationService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return vaRegistrationService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return vaRegistrationService.reject(vo);
	}

	@Validate
	@Input({
			@Variable(name = "mainAccountNo"),
			@Variable(name = "productCode"),
			@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_VALIDATE),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		vaRegistrationService.validateDetail((String) map.get(ApplicationConstants.CORP_ID),
				(String) map.get("mainAccountNo"), (String) map.get("productCode"));
		return map;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CIF_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchCorporateProducts(Map<String, Object> map) throws ApplicationException, BusinessException {		
		return vaRegistrationService.searchCorporateProducts((String) map.get(ApplicationConstants.CIF_ID));
	}
}
