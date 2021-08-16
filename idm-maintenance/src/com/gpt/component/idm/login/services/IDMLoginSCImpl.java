package com.gpt.component.idm.login.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class IDMLoginSCImpl implements IDMLoginSC {
	@Autowired
	private IDMLoginService idmLoginService;
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "passwd"),
		@Variable(name = "key", required = false) //sengaja dibuat false agar tidak di validate 
	})
	@Output({
		@Variable(name = "loginUsername"),
		@Variable(name = "lastLoginDate", type = Date.class, format = Format.DATE_TIME, required = false)
	})
	@Override
	public Map<String, Object> login(Map<String, Object> map) throws ApplicationException, BusinessException {
		if(map.get(ApplicationConstants.LOGIN_HANDLES_LOGOUT) != null) {
			logout(map);
		}
		
		//replacing to plainPasswd
		map.put("passwd", idmLoginService.getPlainPasswd((String) map.get("passwd"), (String) map.get("key")));
		//--------------------------
		
		return idmLoginService.login(map);
	}

	@SuppressWarnings("unchecked")
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_HISTORY_ID, type = List.class) 
	})
	@Override
	public void logout(Map<String, Object> map) throws ApplicationException, BusinessException {
		//remark if stress test
		idmLoginService.logout((ArrayList<String>) map.get(ApplicationConstants.LOGIN_HISTORY_ID),
				(String) map.get(ApplicationConstants.LOGIN_USERID), (Timestamp) map.get(ApplicationConstants.LOGIN_DATE));
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "passwd")
	})
	@Override
	public Map<String, Object> getProfiles(Map<String, Object> map) throws ApplicationException, BusinessException {
		return idmLoginService.getProfiles((String) map.get("loginId"), (String) map.get("passwd"), true);
	}

	@Autowired
	private IDMUserService idmUserService;
	
	@Validate
	@Input({
		@Variable(name = "oldPassword"),
		@Variable(name = "newPassword"),
		@Variable(name = "newPassword2"),
		@Variable(name = "key", required = false), //sengaja dibuat false agar tidak di validate 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"RESET"}), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> forceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		//replacing to plainPasswd
		map.put("oldPassword", idmLoginService.getPlainPasswd((String) map.get("oldPassword"), (String) map.get("key")));
		map.put("newPassword", idmLoginService.getPlainPasswd((String) map.get("newPassword"), (String) map.get("key")));
		map.put("newPassword2", idmLoginService.getPlainPasswd((String) map.get("newPassword2"), (String) map.get("key")));
		//--------------------------
		
		idmUserService.changePassword((String) map.get(ApplicationConstants.LOGIN_USERID), (String) map.get("oldPassword"), (String) map.get("newPassword"),
				(String) map.get("newPassword2"));
		
		Map<String, Object> resultMap = new HashMap<>(1,1);
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100116");
		return resultMap;
	}
	
}
