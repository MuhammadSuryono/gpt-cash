package com.gpt.component.maintenance.branch.services;

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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class BranchSCImpl implements BranchSC {

	@Autowired
	private BranchService branchService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "cityCode"), 
		@Variable(name = "cityName"), 
		@Variable(name = "levelCode"),
		@Variable(name = "levelName"),
		@Variable(name = "contactName"),
		@Variable(name = "dscp"),
		@Variable(name = "email", format = Format.EMAIL),
		@Variable(name = "parentBranchCode", required = false),
		@Variable(name = "address1", required = false), @Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_CREATE,
			ApplicationConstants.WF_ACTION_UPDATE
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return branchService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return branchService.reject(vo);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DELETE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchLevelForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_LEVEL"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCityForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_CITY"); 
		return parameterMaintenanceService.searchModel(map);
	}
}
