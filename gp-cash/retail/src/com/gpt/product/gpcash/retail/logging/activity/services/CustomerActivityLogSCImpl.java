package com.gpt.product.gpcash.retail.logging.activity.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gpt.component.common.Constants;
import com.gpt.component.common.MDCHelper;
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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.logging.activity.valueobject.CustomerActivityLogVO;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;

@Validate
@Service
public class CustomerActivityLogSCImpl implements CustomerActivityLogSC {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerActivityLogService customerActivityService;
	
	@Override
	@Async("ActivityLogTaskExecutor")
	public void saveActivityLog(String menuCode, String menuName, String action, boolean isError, String errorCode, String errorDescription, String errorTrace, String referenceNo, String customerId, Timestamp activityDate, String logId) {
		MDCHelper.put(Constants.KEY_MDC_TRACE_ID, logId);
		
		CustomerActivityLogVO vo = new CustomerActivityLogVO();
		try{
			vo.setMenuCode(menuCode);
			vo.setMenuName(menuName);
			vo.setActivityDate(activityDate);
		    vo.setActionType(action);
		    vo.setActionBy(customerId);
		    vo.setReferenceNo(referenceNo);
		        
		    if(isError){
				vo.setError(true);
				vo.setErrorCode(errorCode);
				vo.setErrorTrace(errorTrace);
				vo.setErrorDescription(errorDescription);
		    }
		        
		    customerActivityService.saveCustomerActivityLog(vo);
		}catch(Exception e){
			logger.debug(" error saveCustomerActivityLog : " + e.getMessage());
		}
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = "actionBy", alias = ApplicationConstants.LOGIN_USERCODE),
		@Variable(name = "activityLogMenuCode", required = false, format = Format.UPPER_CASE),
		@Variable(name = "actionType", required = false, format = Format.UPPER_CASE),
		@Variable(name = "fromDateVal", required = false, type = Date.class, format = Format.DATE),
		@Variable(name = "toDateVal", required = false, type = Date.class, format = Format.DATE),
	})
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerActivityService.getActivityByUser(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = "fromDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "toDateVal", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "actionType", required = false),
		@Variable(name = "menuType"),
		@Variable(name = "actionByUserId", required = false),
		@Variable(name = "status", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "customerId"),
			@SubVariable(name = "customerName"),
			@SubVariable(name = "actionDate", format = Format.DATE),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
			@SubVariable(name = "activityLogMenuCode"),
			@SubVariable(name = "activityLogMenuName"),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
			@SubVariable(name = "actionByUserId"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return customerActivityService.getNonFinancialActvity(map);
	}
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = 
			@SortingItem(name = "activityDate", alias = "actionDate")
	)
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskId", required = false),
			@SubVariable(name = "customerId"),
			@SubVariable(name = "customerName"),
			@SubVariable(name = "actionDate"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false),
			@SubVariable(name = "activityLogMenuCode"),
			@SubVariable(name = "activityLogMenuName"),
			@SubVariable(name = "actionType"),
			@SubVariable(name = "uniqueKeyDisplay", required = false),
			@SubVariable(name = "actionByUserId"),
			@SubVariable(name = "actionByUserName"),
			@SubVariable(name = "status", format = Format.I18N),
		})			
	})	
	@Override
	public Map<String, Object> searchByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return customerActivityService.getNonFinancialActvity(map);
	}
	
}
