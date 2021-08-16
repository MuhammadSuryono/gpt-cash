package com.gpt.product.gpcash.menupackage.services;

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
import com.gpt.component.idm.menutree.services.IDMMenuTreeService;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@Validate
@Service
@Transactional(rollbackFor = Exception.class)
public class MenuPackageSCImpl implements MenuPackageSC{

	@Autowired
	private MenuPackageService menuPackageService;
	
	@Autowired
	private IDMMenuTreeService idmMenuTreeService;
	
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "menuList", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.STR_MENUCODE),
			@SubVariable(name = "menuName")
		}),
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
		return menuPackageService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return menuPackageService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return menuPackageService.reject(vo);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return menuPackageService.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchMenuPackageDetailByCode(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return menuPackageService.searchMenuPackageDetailByCode(map);
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
		return menuPackageService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
	})	
	@Override
	public Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return idmMenuTreeService.search(map);
	}
}
