package com.gpt.component.maintenance.exchangerate.services;

import java.sql.Timestamp;
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
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class ExchangeRateSCImpl implements ExchangeRateSC{
	
	@Autowired
	private ExchangeRateService exchangeRateService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "currencyCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH, 
			ApplicationConstants.WF_ACTION_DETAIL
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output(
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "id"),
			@SubVariable(name = "effectiveDate", type = Timestamp.class),
			@SubVariable(name = "currencyQuantity", required = false),
			@SubVariable(name = "currencyCode"),
			@SubVariable(name = "currencyName"),
			@SubVariable(name = "transactionBuyRate", required = false),
			@SubVariable(name = "transactionSellRate", required = false),
			@SubVariable(name = "transactionMidRate", required = false),
		})
	)
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return exchangeRateService.search(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "currencyCode"),	
		@Variable(name = "trxBuyRate"),	
		@Variable(name = "trxMidRate"),	
		@Variable(name = "trxSellRate"),	
		@Variable(name = "bankBuyRate", required = false),
		@Variable(name = "bankSellRate", required = false),
		@Variable(name = "tellerBuyRate", required = false),
		@Variable(name = "tellerSellRate", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_CREATE, 
			ApplicationConstants.WF_ACTION_UPDATE,
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
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return exchangeRateService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return exchangeRateService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return exchangeRateService.reject(vo);
	}

}
