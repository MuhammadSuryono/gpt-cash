package com.gpt.product.gpcash.corporate.logging.activity.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gpt.component.common.Constants;
import com.gpt.component.common.MDCHelper;
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
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.logging.activity.valueobject.CorporateActivityLogVO;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;

@Validate
@Service
public class CorporateActivityLogSCImpl implements CorporateActivityLogSC {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CorporateActivityLogService corporateActivityService;
	
	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;

	@Override
	@Async("ActivityLogTaskExecutor")
	public void saveActivityLog(String menuCode, String menuName, String action, String loginId, boolean isError, String errorCode, String errorDescription, String errorTrace, String referenceNo, String corporateId, Timestamp activityDate, String logId) {
		MDCHelper.put(Constants.KEY_MDC_TRACE_ID, logId);
		
		CorporateActivityLogVO vo = new CorporateActivityLogVO();
		try{
			vo.setMenuCode(menuCode);
			vo.setMenuName(menuName);
			vo.setActivityDate(activityDate);
		    vo.setActionType(action);
		    vo.setActionBy(loginId);
		    vo.setReferenceNo(referenceNo);
		    vo.setCorporateId(corporateId);
		        
		    if(isError){
				vo.setError(true);
				vo.setErrorCode(errorCode);
				vo.setErrorTrace(errorTrace);
				vo.setErrorDescription(errorDescription);
		    }
		        
		    corporateActivityService.saveCorporateActivityLog(vo);
		}catch(Exception e){
			logger.debug(" error saveCorporateActivityLog : " + e.getMessage());
		}
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = "actionBy", alias = ApplicationConstants.LOGIN_USERCODE),
		@Variable(name = "activityLogMenuCode", required = false, format = Format.UPPER_CASE),
		@Variable(name = "actionType", required = false, format = Format.UPPER_CASE),
		@Variable(name = "fromDateVal", required = false, type = Date.class, format = Format.DATE),
		@Variable(name = "toDateVal", required = false, type = Date.class, format = Format.DATE),
	})
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateActivityService.getActivityByUser(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionType", required = false),
		@Variable(name = "menuType"),
		@Variable(name = "actionByUserId", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateActivityService.getNonFinancialActvity(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateActivityService.getNonFinancialActvity(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		return corporateUserService.findUserByCorporate((String)map.get(ApplicationConstants.LOGIN_CORP_ID));
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionType", required = false),
		@Variable(name = "menuType"),
		@Variable(name = "actionByUserId", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DOWNLOAD),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateActivityService.downloadActivity(map);
	}
}
