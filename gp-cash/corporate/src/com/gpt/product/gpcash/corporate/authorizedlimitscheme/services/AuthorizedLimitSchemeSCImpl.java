package com.gpt.product.gpcash.corporate.authorizedlimitscheme.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

@Validate
@Service
public class AuthorizedLimitSchemeSCImpl implements AuthorizedLimitSchemeSC {

	@Autowired
	private AuthorizedLimitSchemeService authorizedLimitSchemeService;

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "approvalLevelCode", format = Format.UPPER_CASE), 
		@Variable(name = "approvalLevelName"), 
		@Variable(name = "alias"),
		@Variable(name = "currencyCode"),
		@Variable(name = "currencyName"),
		@Variable(name = "makerLimit"),
		@Variable(name = "singleApprovalLimit"),
		@Variable(name = "intraGroupLimit"),
		@Variable(name = "crossGroupLimit"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.submit(map);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.approve(vo);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.reject(vo);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "approvalLevelCode", format = Format.UPPER_CASE), 
		@Variable(name = "approvalLevelName"), 
		@Variable(name = "alias"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.submit(map);
	}

}