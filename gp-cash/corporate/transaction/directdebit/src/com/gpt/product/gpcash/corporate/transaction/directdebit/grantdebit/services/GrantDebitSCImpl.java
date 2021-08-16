package com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.services;

import java.math.BigDecimal;
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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class GrantDebitSCImpl implements GrantDebitSC {

	@Autowired
	private GrantDebitService grantDebitService;

	@Autowired
	private CorporateService corporateService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, required = false, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.STR_NAME, required = false),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.search(map);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return grantDebitService.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = "accountList", type = List.class, subVariables = { @SubVariable(name = "accountNo"),
					@SubVariable(name = "accountName"), @SubVariable(name = "accountCurrencyCode"),
					@SubVariable(name = "expiryDate", type = Date.class, format = Format.DATE, required = false),
					@SubVariable(name = "maxDebitLimit", type = BigDecimal.class) }),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return grantDebitService.submit(map);
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
		return grantDebitService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return grantDebitService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return grantDebitService.reject(vo);
	}

	@Validate
	@Input({
			@Variable(name = "accountNo"),
			@Variable(name = "expiryDate", type = Date.class, format = Format.DATE, required = false),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_VALIDATE),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ 
			@Variable(name = "accountNo"),
			@Variable(name = "accountName"),
			@Variable(name = "accountCurrencyCode") 
	})
	@Override
	public Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		return grantDebitService.validateDetail((String) map.get("accountNo"), (Date) map.get("expiryDate"));
	}
}
