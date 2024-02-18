package com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.customer.services.CustomerService;

@Validate
@Service
public class CustomerUnderlyingDocumentSCImpl implements CustomerUnderlyingDocumentSC {

	@Autowired
	private CustomerUnderlyingDocumentService underlyingService;

	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ @Variable(name = ApplicationConstants.CUST_ID, required = false, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.STR_NAME, required = false),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> searchCustomer(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.search(map);
	}

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ @Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return underlyingService.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
			@Variable(name = "documentList", type = List.class, subVariables = {
					@SubVariable(name = "documentTypeCode"),
					@SubVariable(name = "underlyingAmount", type = BigDecimal.class), 
					@SubVariable(name = "underlyingCcy"),
					@SubVariable(name = "branchCode"),
					@SubVariable(name = "expiryDate", type = Date.class, format = Format.DATE),
					@SubVariable(name = "remark", required = false) }),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return underlyingService.submit(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ @Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
			@Variable(name = "documentList", type = List.class, subVariables = { @SubVariable(name = "id") }),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DELETE),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({ @Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
			@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) })
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return underlyingService.submit(map);
	}

	@Validate
	@Input({
			@Variable(name = "docTypeCode"),
			@Variable(name = "underlyingAmount"),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_VALIDATE),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		underlyingService.validateDetail((String) map.get("docTypeCode"), (String) map.get("underlyingAmount"));
		return map;
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
	public Map<String, Object> searchDocumentTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_DOCUMENT_TYP");
		return parameterMaintenanceService.searchModel(map);
	}
}
