package com.gpt.product.gpcash.chargeupdate.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.chargepackage.services.ChargePackageService;

@Validate
@Service
@Transactional(rollbackFor = Exception.class)
public class ChargeUpdateSCImpl implements ChargeUpdateSC {
	
	@Autowired
	private ChargePackageService chargePackageService;
	
	@Autowired
	private ChargeUpdateService chargeUpdateService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchAll(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return chargePackageService.searchServiceChargeTRX(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchSpesific(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chargePackageService.searchSpesific((String) map.get(ApplicationConstants.STR_CODE), Helper.getActiveApplicationCode());
	}
	
	@Validate(sorting = Option.REQUIRED)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH 
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchAllChargePackage(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return chargePackageService.searchForDroplist(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "isUpdateAll", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isUpdateCorporateWithSpecialCharge", required = false, options = {
				ApplicationConstants.YES, 
				ApplicationConstants.NO
		}),
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE, required = false),
		@Variable(name = "list", type = List.class, subVariables = {
			@SubVariable(name = "serviceChargeId"),
			@SubVariable(name = "currencyCode"),
			@SubVariable(name = "value", type = BigDecimal.class)
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
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
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return chargeUpdateService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCurrencyForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_CURRENCY"); 
		return parameterMaintenanceService.searchModel(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return chargeUpdateService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return chargeUpdateService.reject(vo);
	}

}
