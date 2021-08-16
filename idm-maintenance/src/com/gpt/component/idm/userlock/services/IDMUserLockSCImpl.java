package com.gpt.component.idm.userlock.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class IDMUserLockSCImpl implements IDMUserLockSC{
	
	@Autowired
	private IDMUserLockService idmUserLockService;

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UNLOCK), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "userList", type = List.class)
	})
	@Override
	public void unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserLockService.unlockUser(map);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "status", options = ApplicationConstants.IDM_USER_STATUS_LOCKED), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return idmUserLockService.search(map);
	}

}
