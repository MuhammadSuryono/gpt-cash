package com.gpt.component.idm.user.services;

import java.util.Date;
import java.util.HashMap;
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
import com.gpt.component.idm.role.services.IDMRoleService;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.branch.services.BranchService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class IDMUserSCImpl implements IDMUserSC {

	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private BranchService branchService;
	
	@Autowired
	private IDMRoleService roleService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false, format = Format.UPPER_CASE),
		@Variable(name = "branchCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH, 
			ApplicationConstants.WF_ACTION_DETAIL
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return idmUserService.searchIDMUserForBackOffice(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "branchCode"),
		@Variable(name = "activeFrom", format = Format.DATE_TIME, type = Date.class),
		@Variable(name = "activeTo", required = false, format = Format.DATE_TIME, type = Date.class),
		@Variable(name = "isPwdNeverExpired", options = {
			ApplicationConstants.YES, 
			ApplicationConstants.NO
		}),
		@Variable(name = "roleCodeList", type = List.class, subVariables = {
			@SubVariable(name = "roleCode")
		}),
		@Variable(name = "email"),
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
		return idmUserService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return idmUserService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return idmUserService.reject(vo);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
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
		return idmUserService.submit(map);
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchRole(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHBO);
		return roleService.search(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UNLOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.unlockUser((String) map.get(ApplicationConstants.STR_CODE), (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100081");
		return resultMap;
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_LOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.lockUser((String) map.get(ApplicationConstants.STR_CODE), (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100085");
		return resultMap;
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_RESET}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.resetUser((String) map.get(ApplicationConstants.STR_CODE), (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100082");
		return resultMap;
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_INACTIVATE}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> inactivateUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.inactivateUser((String) map.get(ApplicationConstants.STR_CODE), (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100083");
		return resultMap;
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_ACTIVATE}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> activateUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.activateUser((String) map.get(ApplicationConstants.STR_CODE), (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100084");
		return resultMap;
	}
}
