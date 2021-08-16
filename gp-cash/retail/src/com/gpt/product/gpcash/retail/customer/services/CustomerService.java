package com.gpt.product.gpcash.retail.customer.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@AutoDiscoveryImpl
public interface CustomerService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveCustomer(CustomerModel customer, String createdBy, boolean isNeedFlush) throws ApplicationException, BusinessException;
	
	void updateCustomer(CustomerModel customer, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteCustomer(CustomerModel customer, String deletedBy) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCustomers() throws ApplicationException, BusinessException;

	Map<String, Object> customerRegistration(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> customerVerification(Map<String, Object> map) throws BusinessException, ApplicationException;

	Map<String, Object> customerVerification2(Map<String, Object> map) throws BusinessException, ApplicationException;

	void validateRegistrationUserId(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<String> findListOfStringNonFinancialMenu(String customerId, String nonFinMenuType) throws ApplicationException;

	Map<String, Object> findMenuForPendingTask(String customerId) throws ApplicationException;

	Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findLimitUsage(String customerId) throws ApplicationException, BusinessException;

	void updateUserNotificationFlag(String customerId, String notifyMyTrx)
			throws ApplicationException, BusinessException;

	Map<String, Object> forgotUserId(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	void validateRegistrationExistingUserId(Map<String, Object> map) throws ApplicationException, BusinessException;

	CustomerModel findByUserIdContainingIgnoreCase(String customerId) throws ApplicationException;

	Map<String, Object> customerVerificationForExistingUser(Map<String, Object> map)
			throws BusinessException, ApplicationException;
}