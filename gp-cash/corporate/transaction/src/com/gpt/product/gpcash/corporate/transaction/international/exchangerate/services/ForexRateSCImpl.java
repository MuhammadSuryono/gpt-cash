package com.gpt.product.gpcash.corporate.transaction.international.exchangerate.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.exchangerate.services.ExchangeRateService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;

@Validate
@Service
public class ForexRateSCImpl implements ForexRateSC {

	@Autowired
	private ExchangeRateService exchangeRateService;
	
	@EnableCorporateActivityLog
	@Input({ @Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return exchangeRateService.search(map);
	}

}
