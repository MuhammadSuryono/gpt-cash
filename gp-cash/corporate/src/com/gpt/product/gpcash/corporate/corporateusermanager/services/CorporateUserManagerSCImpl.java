package com.gpt.product.gpcash.corporate.corporateusermanager.services;

import java.util.Arrays;
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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;

@Validate
@Service
public class CorporateUserManagerSCImpl implements CorporateUserManagerSC {

	@Autowired
	private CorporateUserManagerService corporateUserManagerService;
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.CORP_ID),
		@Variable(name = "stillLoginFlag", options = { ApplicationConstants.YES, ApplicationConstants.NO}), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> findByStillLogin(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return corporateUserManagerService.findByStillLogin(map);
	}

	@EnableCorporateActivityLog
	@Override
	public Map<String, Object> findByLockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserManagerService.findByLockUser(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "userList", type = List.class)
	})
	@Override
	public void updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		corporateUserManagerService.updateStillLoginFlag(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public void updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Arrays.asList(ApplicationConstants.APP_GPCASHIB, ApplicationConstants.APP_GPCASHIB_ADMIN));
		corporateUserManagerService.updateStillLoginFlagALL(map);
	}

}
