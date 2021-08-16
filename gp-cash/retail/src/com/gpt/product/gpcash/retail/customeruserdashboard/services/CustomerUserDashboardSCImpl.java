package com.gpt.product.gpcash.retail.customeruserdashboard.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.cot.system.services.SystemCOTService;
import com.gpt.product.gpcash.retail.customer.services.CustomerService;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.usertransactionmapping.services.CustomerUserTransactionMappingService;

@Validate
@Service
public class CustomerUserDashboardSCImpl implements CustomerUserDashboardSC {

	@Autowired
	private CustomerUserDashboardService customerUserDashboardService;
	
	@Autowired
	private CustomerUserTransactionMappingService userTransactionMappingService;
	
	@Autowired
	private SystemCOTService systemCOTService;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> searchCustomerAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerUserDashboardService.getCountCustomerAccount((String) map.get(ApplicationConstants.CUST_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> countTotalCreatedTrx(Map<String, Object> map) throws ApplicationException, BusinessException {
		return userTransactionMappingService.countTotalCreatedTrx((String) map.get(ApplicationConstants.CUST_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE)
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> countTotalExecutedTrx(Map<String, Object> map) throws ApplicationException, BusinessException {
		return userTransactionMappingService.countTotalExecutedTrx((String) map.get(ApplicationConstants.CUST_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables= {
			@SubVariable(name = "currencyCode"),
			@SubVariable(name = "currencyName"),
			@SubVariable(name = "maxAmountLimit", type=BigDecimal.class),
			@SubVariable(name = "amountLimitUsage", type=BigDecimal.class)
		})
	})
	@Override
	public Map<String, Object> findLimitUsage(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.findLimitUsage((String) map.get(ApplicationConstants.CUST_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables= {
			@SubVariable(name = ApplicationConstants.STR_NAME),
			@SubVariable(name = "startTime"),
			@SubVariable(name = "endTime")
		})
	})
	@Override
	public Map<String, Object> findCOT(Map<String, Object> map) throws ApplicationException, BusinessException {
		return systemCOTService.findByApplicationCodeSortByEndTime(ApplicationConstants.APP_GPCASHIB_R);
	}
	
	@Validate
	@Input({
		@Variable(name = "isNotifyMyTrx", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UPDATE}),
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> updateUserNotificationFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		customerService.updateUserNotificationFlag((String) map.get(ApplicationConstants.CUST_ID), 
				(String) map.get("isNotifyMyTrx"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100152");
		return resultMap;
	}
	
	@Validate
	@Output({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME)
	})
	@Override
	public Map<String, Object> uploadAvatar(Map<String, Object> map) throws ApplicationException, BusinessException {
		return idmUserService.uploadAvatar((byte[]) map.get("rawdata"), 
				(String) map.get(ApplicationConstants.FILENAME));
	}
	
	@Validate
	@Input({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"UPLOAD_PROFILE"}),
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> saveAvatar(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmUserService.saveAvatar((String) map.get(ApplicationConstants.CUST_ID), 
				(String) map.get(ApplicationConstants.FILENAME), 
				(String) map.get("fileId"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100154");
		return resultMap;
	}
}