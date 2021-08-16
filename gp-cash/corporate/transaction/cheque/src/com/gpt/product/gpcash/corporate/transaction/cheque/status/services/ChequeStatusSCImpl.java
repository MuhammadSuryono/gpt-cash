package com.gpt.product.gpcash.corporate.transaction.cheque.status.services;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.constants.ChequeConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.services.ChequeOrderService;

@Validate
@Service
public class ChequeStatusSCImpl implements ChequeStatusSC {
	@Autowired
	private ChequeOrderService chequeOrderService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "orderNo", required = false),
		@Variable(name = "statusCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.searchForBank(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ChequeConstants.WF_ACTION_PROCESS
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.process(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = "chequeSerial"),
		@Variable(name = "chequeNoFrom"),
		@Variable(name = "chequeNoTo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ChequeConstants.WF_ACTION_SUBMIT_PROCESS
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitProcess(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.submitProcess(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = "chequeSerial"),
		@Variable(name = "chequeNoFrom"),
		@Variable(name = "chequeNoTo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ChequeConstants.WF_ACTION_CONFIRM_PROCESS
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.confirmProcess(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = "reason"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ChequeConstants.WF_ACTION_SUBMIT_DECLINE
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.decline(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "chequeOrderId"),
		@Variable(name = "identityTypeCode"),
		@Variable(name = "identityNo"),
		@Variable(name = "identityName"),
		@Variable(name = "mobileNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ChequeConstants.WF_ACTION_SUBMIT_HANDOVER
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> handover(Map<String, Object> map) throws ApplicationException, BusinessException {
		return chequeOrderService.handover(map);
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
	public Map<String, Object> searchIdentityTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_IDENTITY_TYP"); 
		return parameterMaintenanceService.searchModel(map);
	}
}

