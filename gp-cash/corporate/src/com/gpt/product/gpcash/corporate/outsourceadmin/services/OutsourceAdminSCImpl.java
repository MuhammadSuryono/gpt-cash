package com.gpt.product.gpcash.corporate.outsourceadmin.services;

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
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class OutsourceAdminSCImpl implements OutsourceAdminSC{
	
	@Autowired
	private OutsourceAdminService outsourceAdminService;
	
	@Autowired
	private CorporateService corporateService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, required = false, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {
				menuCode,
				"MNU_GPCASH_OUTSOURCE_ADMIN"
		})
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		Map <String, Object>returnMap = corporateService.search(map);
	
		//save to temp table for login
		Map <String, Object>returnMap2 = outsourceAdminService.saveForLogin(map);
		
		returnMap.putAll(returnMap2);
		return returnMap;
	}

}
