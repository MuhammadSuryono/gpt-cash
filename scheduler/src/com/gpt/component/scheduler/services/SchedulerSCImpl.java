package com.gpt.component.scheduler.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SortingItem;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class SchedulerSCImpl implements SchedulerSC {
	
	@Autowired
	private SchedulerService schedulerService;

	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = @SortingItem(name="schedulerTask.code", alias="taskCode"))
	@Input({ 
		@Variable(name = "taskCode", required = false, format = Format.UPPER_CASE),
		@Variable(name = "taskName", required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return schedulerService.search(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "taskCode", required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> searchStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return schedulerService.searchStatus(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "schedulerId"),
		@Variable(name = "cron"),
		@Variable(name = "parameter", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		schedulerService.submit(map);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100121");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "taskCode", format = Format.UPPER_CASE),
		@Variable(name = "parameter", required = false, defaultValue = ""),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_EXECUTE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})
	@Override
	public Map<String, Object> execute(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		schedulerService.execute((String) map.get("taskCode"), 
				(String) map.get(ApplicationConstants.LOGIN_USERID), false, (String) map.get("parameter"));
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100122");
		return resultMap;
	}
	
}
