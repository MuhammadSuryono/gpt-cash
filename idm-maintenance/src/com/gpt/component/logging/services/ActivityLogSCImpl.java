package com.gpt.component.logging.services;

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
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.logging.valueobject.ActivityLogVO;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class ActivityLogSCImpl implements ActivityLogSC {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ActivityLogService activityLogService;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Override
	@Async("ActivityLogTaskExecutor")
	public void saveActivityLog(String menuCode, String menuName, String action, String loginId, boolean isError, String errorCode,
			String errorDescription, String errorTrace, String referenceNo, Timestamp activityDate, String logId) {
		MDCHelper.put(Constants.KEY_MDC_TRACE_ID, logId);
		
		ActivityLogVO vo = new ActivityLogVO();
		try {
			vo.setMenuCode(menuCode);
			vo.setMenuName(menuName);
			vo.setActivityDate(activityDate);
			vo.setActionType(action);
			vo.setActionBy(loginId);
			vo.setReferenceNo(referenceNo);

			if (isError) {
				vo.setError(true);
				vo.setErrorCode(errorCode);
				vo.setErrorTrace(errorTrace);
				vo.setErrorDescription(errorDescription);
			}

			activityLogService.saveActivityLog(vo);
		} catch (Exception e) {
			logger.debug(" error  saveActivityLog : " + e.getMessage());
		}
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
	})
	@Override
	public Map<String, Object> getMenuForActivityLog(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return activityLogService.getMenuForActivityLog(map);
	}

	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionBy", required = false),
		@Variable(name = "activityLogMenuCode", required = false),
		@Variable(name = "actionType", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "actionBy"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "actionDate", type = Timestamp.class),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "isError"),
			@SubVariable(name = "status"),
			@SubVariable(name = "errorCode", required = false),
			@SubVariable(name = "errorDescription", required = false),
			@SubVariable(name = ApplicationConstants.STR_MENUCODE),
			@SubVariable(name = "menuName"),
			@SubVariable(name = "referenceNo", required = false),
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
		})
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return activityLogService.search(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "actionBy"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "actionDate", type = Timestamp.class),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "isError"),
			@SubVariable(name = "status"),
			@SubVariable(name = "errorCode", required = false),
			@SubVariable(name = "errorDescription", required = false),
			@SubVariable(name = ApplicationConstants.STR_MENUCODE),
			@SubVariable(name = "menuName"),
			@SubVariable(name = "referenceNo", required = false),
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
		})
	})
	@Override
	public Map<String, Object> searchByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		return activityLogService.search(map);
	}

	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
	})
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("actionBy", map.get(ApplicationConstants.LOGIN_USERID));
		return activityLogService.getActivityByUser(map);
	}
	
	@Validate
	@Input({
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
		return idmUserService.findBankUsers();
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
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionBy", required = false),
		@Variable(name = "activityLogMenuCode", required = false),
		@Variable(name = "actionType", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DOWNLOAD), 
	})
	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException {
		return activityLogService.downloadReport(map);
	}
}
