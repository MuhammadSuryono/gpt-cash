package com.gpt.component.pendingtask.services;

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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class PendingTaskSCImpl implements PendingTaskSC {

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private MultiPendingTaskService multiPendingTaskService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingTaskService.search(map);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = @SortingItem("startDate"))
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "pendingTaskMenuCode", required = false)
	})
	@Override
	public Map<String, Object> searchPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskByUser(map);
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
	})
	@Output({
		@Variable(name = "total", type = Integer.class)
	})
	@Override
	public Map<String, Object> countPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.countPendingTaskByUser(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name="stageId"),
		@Variable(name="approvalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
		@Variable(name="approvalLvCount"),
		@Variable(name="approvalLvRequired"),
		@Variable(name = "startDate", type = Timestamp.class),
		@Variable(name = "tasks", type = List.class, subVariables = {
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName")
		})
	})
	@Override
	public Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskByReferenceNo(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output(
		@Variable(name = "activities", type = List.class, subVariables = {
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName"),
			@SubVariable(name = "activity", format = Format.I18N),
			@SubVariable(name = "activityDate", type = Timestamp.class),
			@SubVariable(name = "approvalLvName", required = false),
			@SubVariable(name = "approvalLvCount", type = Integer.class, required = false),
			@SubVariable(name = "approvalLvRequired", type = Integer.class, required = false),
			@SubVariable(name = "status", format = Format.I18N)
		})
	)
	@Override
	public Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskHistoryByReferenceNo(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = "details", type = Object.class, required = false)
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.detailPendingTask(map);
	}

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = "details", type = Object.class, required = false)
	})
	@Override
	public Map<String, Object> detailPendingTaskOld(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.detailPendingTaskOld(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_APPROVE), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "taskId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingTaskService.approve(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_REJECT), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "taskId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingTaskService.reject(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_APPROVE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "pendingTaskList",  type = List.class, subVariables = {
			@SubVariable(name = "taskId"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE)
		}),
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@SubVariable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
		})
	})
	@Override
	public Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return multiPendingTaskService.approveList(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_REJECT), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
		@Variable(name = "pendingTaskList",  type = List.class, subVariables = {
			@SubVariable(name = "taskId"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE)
		}),
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@SubVariable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
		})
	})
	@Override
	public Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return multiPendingTaskService.rejectList(map);
	}

}
