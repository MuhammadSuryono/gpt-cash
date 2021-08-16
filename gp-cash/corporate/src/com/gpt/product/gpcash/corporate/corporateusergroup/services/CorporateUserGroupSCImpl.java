package com.gpt.product.gpcash.corporate.corporateusergroup.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.gpt.product.gpcash.corporate.corporatelimit.services.CorporateLimitService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

@Validate
@Service
public class CorporateUserGroupSCImpl implements CorporateUserGroupSC {

	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@Autowired
	private CorporateLimitService corporateLimitService;
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
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
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateUserGroupService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = "beneficiaryScope", options = {ApplicationConstants.BEN_SCOPE_USER_GROUP, ApplicationConstants.BEN_SCOPE_CORPORATE}),
		@Variable(name = "accountGroupCode", format = Format.UPPER_CASE), @Variable(name = "accountGroupName"),
		@Variable(name = "menuList", type = List.class, subVariables = { 
			@SubVariable(name = ApplicationConstants.STR_MENUCODE),
			@SubVariable(name = "menuName") 
		}),
		@Variable(name = "limitList", type = List.class, subVariables = { 
			@SubVariable(name = "maxAmountLimit"), @SubVariable(name = "currencyCode"),
			@SubVariable(name = "currencyName"), @SubVariable(name = "serviceCode"),
			@SubVariable(name = "serviceName") 
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_CREATE,
			ApplicationConstants.WF_ACTION_UPDATE 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.submit(map);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		return corporateUserGroupService.approve(vo);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		return corporateUserGroupService.reject(vo);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.submit(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCorporateLimitList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateLimitService.searchCorporateLimitList((String) map.get(ApplicationConstants.LOGIN_CORP_ID), ApplicationConstants.APP_GPCASHIB);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCorporateMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateUserGroupService.searchCorporateMenu(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateUserGroupService.searchCorporateAccountGroup(map);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void resetLimit(String parameter) throws ApplicationException, BusinessException {
		corporateUserGroupService.resetLimit(parameter);
	}
}
