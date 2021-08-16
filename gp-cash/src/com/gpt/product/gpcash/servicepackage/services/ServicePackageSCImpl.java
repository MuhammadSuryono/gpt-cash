package com.gpt.product.gpcash.servicepackage.services;

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
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.chargepackage.services.ChargePackageService;
import com.gpt.product.gpcash.limitpackage.services.LimitPackageService;
import com.gpt.product.gpcash.menupackage.services.MenuPackageService;

@Validate
@Service
public class ServicePackageSCImpl implements ServicePackageSC{

	@Autowired
	private ServicePackageService servicePackageService;
	
	@Autowired
	private LimitPackageService limitPackageService;
	
	@Autowired
	private ChargePackageService chargePackageService;
	
	@Autowired
	private MenuPackageService menuPackageService;
	
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
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return servicePackageService.search(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "chargePackageCode"),
		@Variable(name = "limitPackageCode"),
		@Variable(name = "menuPackageCode"),
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
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return servicePackageService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return servicePackageService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return servicePackageService.reject(vo);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_DELETE
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return servicePackageService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH, 
			ApplicationConstants.WF_ACTION_DETAIL
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCharge(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return chargePackageService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH, 
			ApplicationConstants.WF_ACTION_DETAIL
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchLimit(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return limitPackageService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH, 
			ApplicationConstants.WF_ACTION_DETAIL
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return menuPackageService.search(map);
	}
}
