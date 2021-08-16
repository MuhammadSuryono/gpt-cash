package com.gpt.component.idm.changepassword.services;

import java.util.HashMap;
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
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class ChangePasswordSCImpl implements ChangePasswordSC {
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private IDMLoginService idmLoginService;
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "oldPassword"),
		@Variable(name = "newPassword"),
		@Variable(name = "newPassword2"),
		@Variable(name = "key", required = false), //sengaja dibuat false agar tidak di validate 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UPDATE}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> userChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		//replacing to plainPasswd
		map.put("oldPassword", idmLoginService.getPlainPasswd((String) map.get("oldPassword"), (String) map.get("key")));
		map.put("newPassword", idmLoginService.getPlainPasswd((String) map.get("newPassword"), (String) map.get("key")));
		map.put("newPassword2", idmLoginService.getPlainPasswd((String) map.get("newPassword2"), (String) map.get("key")));
		//--------------------------
		
		idmUserService.changePassword((String) map.get(ApplicationConstants.LOGIN_USERID), (String) map.get("oldPassword"), (String) map.get("newPassword"),
				(String) map.get("newPassword2"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100116");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> getInfo(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			int passwordLength = Integer.parseInt(
					maintenanceRepo.isSysParamValid(SysParamConstants.MIN_PWD_LENGTH).getValue());
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("passwordLength", passwordLength);
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
