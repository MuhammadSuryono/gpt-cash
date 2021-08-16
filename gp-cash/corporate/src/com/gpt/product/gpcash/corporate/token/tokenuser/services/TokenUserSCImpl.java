package com.gpt.product.gpcash.corporate.token.tokenuser.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class TokenUserSCImpl implements TokenUserSC {
	
	@Autowired
	private TokenUserService tokenUserService;
	
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
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = "tokenList", type = List.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return tokenUserService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return tokenUserService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return tokenUserService.reject(vo);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DETAIL}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return tokenUserService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "tokenType"),
		@Variable(name = "tokenNo"),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = "tokenType"),
		@Variable(name = "tokenNo")
	})	
	@Override
	public Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		return tokenUserService.searchToken(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "tokenNo"),
		@Variable(name = "userId"),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = "UNBLOCK"), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> unblockToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
		String userCode = Helper.getCorporateUserCode(corporateId, 
				(String) map.get("userId"));
		return tokenUserService.unblockToken(userCode, (String) map.get("tokenNo"), corporateId);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "tokenNo"),
		@Variable(name = "userId"),
		@Variable(name = "randomLockedCode"),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = "UNLOCK"), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> unlockToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
		String userCode = Helper.getCorporateUserCode(corporateId, 
				(String) map.get("userId"));
		return tokenUserService.unlockToken(userCode, (String) map.get("tokenNo"), corporateId,
				(String) map.get("randomLockedCode"));
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "tokenNo"),
		@Variable(name = "userId"),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "UNASSIGN" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> unassignToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		return tokenUserService.submit(map);
	}
}
