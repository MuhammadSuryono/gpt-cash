package com.gpt.product.gpcash.corporate.transaction.international.status.services;

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
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.transaction.international.constants.InternationalConstants;
import com.gpt.product.gpcash.corporate.transaction.international.services.InternationalTransferService;

@Validate
@Service
public class InternationalTransferStatusSCImpl implements InternationalTransferStatusSC {

	@Autowired
	private InternationalTransferService internationalService;

	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;


	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
			@Variable(name = "referenceNo", required = false),
			@Variable(name = "statusCode", required = false),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = {
					ApplicationConstants.WF_ACTION_SEARCH, 
					ApplicationConstants.WF_ACTION_DETAIL
					}),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalService.searchForBank(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "internationalTransferId"),
		@Variable(name = "transactionId"),
		@Variable(name = "processRemark", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			InternationalConstants.WF_ACTION_PROCESS
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalService.process(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = "internationalTransferId"),
		@Variable(name = "declineRemark"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
				InternationalConstants.WF_ACTION_DECLINE
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalService.decline(map);
	}

	

}
