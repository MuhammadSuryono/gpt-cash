package com.gpt.product.gpcash.corporate.corporateuserdashboard.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.maintenance.promo.services.PromoService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.usertransactionmapping.services.UserTransactionMappingService;
import com.gpt.product.gpcash.cot.system.services.SystemCOTService;

@Validate
@Service
public class CorporateUserDashboardSCImpl implements CorporateUserDashboardSC {

	@Autowired
	private CorporateUserDashboardService corporateUserDashboardService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private UserTransactionMappingService userTransactionMappingService;
	
	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@Autowired
	private SystemCOTService systemCOTService;
	
	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private PromoService promoService;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCorporateUserGroupAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserDashboardService.getCountCorporateIdAndGroupId((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCountPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.countPendingTaskByUser(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> countTotalCreatedTrx(Map<String, Object> map) throws ApplicationException, BusinessException {
		return userTransactionMappingService.countTotalCreatedTrx((String) map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> countTotalExecutedTrx(Map<String, Object> map) throws ApplicationException, BusinessException {
		return userTransactionMappingService.countTotalExecutedTrx((String) map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables= {
			@SubVariable(name = "currencyCode"),
			@SubVariable(name = "currencyName"),
			@SubVariable(name = "maxAmountLimit", type=BigDecimal.class),
			@SubVariable(name = "amountLimitUsage", type=BigDecimal.class)
		})
	})
	@Override
	public Map<String, Object> findLimitUsage(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.findDetailByUserGroupId((String) map.get(ApplicationConstants.LOGIN_CORP_ID),(String) map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables= {
			@SubVariable(name = ApplicationConstants.STR_NAME),
			@SubVariable(name = "startTime"),
			@SubVariable(name = "endTime")
		})
	})
	@Override
	public Map<String, Object> findCOT(Map<String, Object> map) throws ApplicationException, BusinessException {
		return systemCOTService.findByApplicationCodeSortByEndTime(ApplicationConstants.APP_GPCASHIB);
	}
	
	@Validate
	@Input({
		@Variable(name = "isNotifyMyTask", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isNotifyMyTrx", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UPDATE}),
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> updateUserNotificationFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		corporateUserService.updateUserNotificationFlag((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get("isNotifyMyTask"), 
				(String) map.get("isNotifyMyTrx"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100152");
		return resultMap;
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
		idmUserService.saveAvatar((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.FILENAME), 
				(String) map.get("fileId"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100154");
		return resultMap;
	}

	@Override
	public Map<String, Object> getPromo(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("infoType", "PROMO");
		return promoService.search(map);
	}
}