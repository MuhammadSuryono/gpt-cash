package com.gpt.product.gpcash.corporate.transactionholidayupdate.services;

import java.math.BigDecimal;
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
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;

@Validate
@Service
public class TransactionHolidayUpdateSCImpl implements TransactionHolidayUpdateSC {
	
	@Autowired
	private TransactionHolidayUpdateService transactionHolidayService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
			@SortingItem("referenceNo")
		})
		@Input({
			@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
			@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
			@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
			@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
		})
		@Output({
			@Variable(name = "result", type = List.class, subVariables = {
				@SubVariable(name = "pendingTaskId"),
				@SubVariable(name = "corporateId"),
				@SubVariable(name = "corporateName"),
				@SubVariable(name = "createdByUserId"),
				@SubVariable(name = "createdByUserName"),
				@SubVariable(name = "referenceNo"),
				@SubVariable(name = "menuCode"),
				@SubVariable(name = "menuName"),
				@SubVariable(name = "sourceAccount", required = false),
				@SubVariable(name = "sourceAccountName", required = false),
				@SubVariable(name = "sourceAccountCurrencyCode", required = false),
				@SubVariable(name = "sourceAccountCurrencyName", required = false),
				@SubVariable(name = "transactionAmount", required = false, type = BigDecimal.class),
				@SubVariable(name = "totalDebitAmountEquivalent", required = false, type = BigDecimal.class),
				@SubVariable(name = "transactionCurrency", required = false),
				@SubVariable(name = "status", format = Format.I18N),
			})			
		})	
		@Override
		public Map<String, Object> searchHolidayTransactionByReferenceNo(Map<String, Object> map)
				throws Exception {
			return transactionHolidayService.searchHolidayTransactionByReferenceNo(map);
		}


	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE_STATUS}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionHolidayService.submit(map);
	}


	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return transactionHolidayService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return transactionHolidayService.reject(vo);
	}


	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = pendingTaskService.detailPendingTask(map);
		
		return resultMap;
	}	

}
