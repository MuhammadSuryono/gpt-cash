package com.gpt.product.gpcash.corporate.corporatecharge.services;

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
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;

@Validate
@Service
public class CorporateChargeSCImpl implements CorporateChargeSC {

	@Autowired
	private CorporateChargeService corporateChargeService;
	
	@Autowired
	private CorporateService corporateService;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
		return corporateChargeService.search(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID),
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = "corporateChargeList", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "value"),
			@SubVariable(name = "currencyCode")
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateChargeService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateChargeService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateChargeService.reject(vo);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = @SortingItem(name = "id", alias = ApplicationConstants.CORP_ID))
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID,  required = false, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.search(map);
	}
}

