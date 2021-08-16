package com.gpt.product.gpcash.cot.system.services;

import java.util.ArrayList;
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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@Validate
@Service
public class SystemCOTSCImpl implements SystemCOTSC {

	@Autowired
	private SystemCOTService systemCOTService;

	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return systemCOTService.search(map);
	}

	@SuppressWarnings("unchecked")
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "systemCOTList", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE), 
			@SubVariable(name = ApplicationConstants.STR_NAME), 
			@SubVariable(name = "startHour"), 
			@SubVariable(name = "startMinutes"),
			@SubVariable(name = "endHour"), 
			@SubVariable(name = "endMinutes")
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		//put application code
		List<Map<String, Object>> sysParamList = (ArrayList<Map<String,Object>>) map.get("systemCOTList");
		for(Map<String, Object> sysParamMap : sysParamList) {
			sysParamMap.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		}
		return systemCOTService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return systemCOTService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return systemCOTService.reject(vo);
	}
	
	@Override
	public Map<String, Object> validateCOT(Map<String, Object> map) throws Exception {
		String code = (String)map.get(ApplicationConstants.STR_CODE);
		String applicationCode = (String)map.get("applicationCode");
		systemCOTService.validateSystemCOT(code, applicationCode);;
		return map;
	}
}