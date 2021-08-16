package com.gpt.product.gpcash.corporate.corporateadmindashboard.services;

import java.util.Date;
import java.util.HashMap;
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
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.activity.services.CorporateActivityLogService;

@Validate
@Service
public class CorporateAdminDashboardSCImpl implements CorporateAdminDashboardSC {
	
	@Autowired
	private CorporateAdminDashboardService corporateAdminDashboardService;
	
	@Autowired
	private CorporateActivityLogService corporateActivityLogService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAdminDashboardService.searchCorporateAccountGroup((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCorporateUserGroup(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAdminDashboardService.searchCorporateUserGroup((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCorporateAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAdminDashboardService.searchCorporateAccount((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCorporateUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAdminDashboardService.searchCorporateUser((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = "actionBy", alias = ApplicationConstants.LOGIN_USERCODE),
		@Variable(name = "activityLogMenuCode", required = false, format = Format.UPPER_CASE),
		@Variable(name = "actionType", required = false, format = Format.UPPER_CASE),
		@Variable(name = "fromDateVal", required = false, type = Date.class, format = Format.DATE),
		@Variable(name = "toDateVal", required = false, type = Date.class, format = Format.DATE),
	})
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateActivityLogService.getActivityByUser(map);
	}
	
	@Validate
	@Output({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME)
	})
	@Override
	public Map<String, Object> uploadAvatar(Map<String, Object> map) throws ApplicationException, BusinessException {
		return idmUserService.uploadAvatar((byte[]) map.get("rawdata"), 
				(String) map.get(ApplicationConstants.FILENAME));
	}
	
	@Validate
	@Input({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"UPLOAD_PROFILE"}),
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> saveAvatar(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		idmUserService.saveAvatar((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.FILENAME), 
				(String) map.get("fileId"));
		
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100154");
		return resultMap;
	}
}
