package com.gpt.product.gpcash.corporate.nonfinancialforbank.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SortingItem;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.logging.activity.services.CorporateActivityLogService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;

@Validate
@Service
public class NonFinancialForBankSCImpl implements NonFinancialForBankSC{
	@Autowired
	private CorporateActivityLogService corporateActivityLogService;
	
	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionType", required = false),
		@Variable(name = "corporateId"),
		@Variable(name = "menuType"),
		@Variable(name = "actionByUserId", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "corporateId"),
			@SubVariable(name = "corporateName"),
			@SubVariable(name = "actionDate", format = Format.DATE),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
			@SubVariable(name = "activityLogMenuCode"),
			@SubVariable(name = "activityLogMenuName"),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
			@SubVariable(name = "actionByUserId"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})	
	@Override
	public Map<String, Object> search(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return corporateActivityLogService.getNonFinancialActvity(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "corporateId"),
			@SubVariable(name = "corporateName"),
			@SubVariable(name = "actionDate"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
			@SubVariable(name = "activityLogMenuCode"),
			@SubVariable(name = "activityLogMenuName"),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
			@SubVariable(name = "actionByUserId"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})	
	@Override
	public Map<String, Object> searchByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return corporateActivityLogService.getNonFinancialActvity(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.WF_FIELD_PENDING_TASK_ID),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = pendingTaskService.detailPendingTask((String) map.get(ApplicationConstants.WF_FIELD_PENDING_TASK_ID));
		
		return resultMap;
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.CORP_ID),
			@SubVariable(name = "corporateName"),
		})
	})	
	@Override
	public Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.searchCorporates();
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "actionByUserId"),
			@SubVariable(name = "actionByUserName"),
		})
	})	
	@Override
	public Map<String, Object> searchUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserService.findUserByCorporate((String)map.get(ApplicationConstants.CORP_ID));
	}
}
