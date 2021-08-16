package com.gpt.product.gpcash.corporate.transactionupdate.services;

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
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;

@Validate
@Service
public class TransactionUpdateSCImpl implements TransactionUpdateSC {
	@Autowired
	private TransactionStatusService transactionStatusService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private TransactionUpdateService transactionUpdateService;
	
	
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
		@SortingItem("referenceNo"),
		@SortingItem("activityDate"),
		@SortingItem("pendingTaskMenuCode"),
		@SortingItem("sourceAccount"),
		@SortingItem("transactionAmount"),
		@SortingItem("status"),
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
			@SubVariable(name = "pendingTaskMenuCode"),
			@SubVariable(name = "pendingTaskMenuName"),
			@SubVariable(name = "sourceAccount", required = false),
			@SubVariable(name = "sourceAccountName", required = false),
			@SubVariable(name = "sourceAccountCurrencyCode", required = false),
			@SubVariable(name = "sourceAccountCurrencyName", required = false),
			@SubVariable(name = "transactionAmount", required = false, type = BigDecimal.class),
			@SubVariable(name = "transactionCurrency", required = false),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})	
	@Override
	public Map<String, Object> searchTransactionStatusByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.searchTransactionStatusForBank(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "pendingTaskId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "activities", type = List.class, subVariables = {
			@SubVariable(name = "activityDate"),
			@SubVariable(name = "activity", format = Format.I18N),
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName"),
			@SubVariable(name = "amountCcyCd", required = false),
			@SubVariable(name = "amount", required = false, type = BigDecimal.class),
			@SubVariable(name = "approvalLvCount", required = false),
			@SubVariable(name = "approvalLvRequired", required = false),
			@SubVariable(name = "status", format = Format.I18N),
			@SubVariable(name = "isExecute", required = false),
			@SubVariable(name = "isCreate", required = false),
		})			
	})	
	@Override
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.detailTransactionStatus(map);
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
	
	@Validate
	@Input({ 
		@Variable(name = "executedId"),
		@Variable(name = "executedMenuCode"),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return transactionStatusService.detailExecutedTransaction(map);
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
		return transactionUpdateService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return transactionUpdateService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return transactionUpdateService.reject(vo);
	}

}
