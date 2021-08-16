package com.gpt.product.gpcash.corporate.corporateusermanagerforbank.services;

import java.util.Arrays;
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
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;

@Validate
@Service
public class CorporateUserManagerForBankSCImpl implements CorporateUserManagerForBankSC {

	@Autowired
	private CorporateUserManagerForBankService corporateUserManagerForBankService;
	
	@Autowired
	private CorporateService corporateService;
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "userId", required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, required = false),
		@Variable(name = "stillLoginFlag", options = { ApplicationConstants.YES, ApplicationConstants.NO}), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> findByStillLogin(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return corporateUserManagerForBankService.findByStillLogin(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "userList", type = List.class)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		corporateUserManagerForBankService.updateStillLoginFlag(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100086");
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Arrays.asList(ApplicationConstants.APP_GPCASHIB, ApplicationConstants.APP_GPCASHIB_ADMIN));
		corporateUserManagerForBankService.updateStillLoginFlagALL(map);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100086");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, required = false, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchCorporateForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.search(map);
	}

}