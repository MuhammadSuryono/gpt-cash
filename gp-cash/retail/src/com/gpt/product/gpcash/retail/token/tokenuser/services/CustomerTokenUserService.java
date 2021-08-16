package com.gpt.product.gpcash.retail.token.tokenuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;

@AutoDiscoveryImpl
public interface CustomerTokenUserService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	CustomerTokenUserModel saveCustomerTokenUser(String tokenNo, String registeredBy, String customerId, String tokenTypeCode)	throws ApplicationException, BusinessException;

	void deleteCustomerTokenUser(String tokenId) throws ApplicationException, BusinessException;

	Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	void unassignToken(String tokenNo, String customerId)
			throws ApplicationException, BusinessException;

	Map<String, Object> unblockToken(String tokenNo, String customerId)
			throws ApplicationException, BusinessException;

	Map<String, Object> unlockToken(String tokenNo, String customerId, String randomLockedCode)
			throws ApplicationException, BusinessException;

	void assignToken(String tokenNo, String customerId, String createdBy)
			throws ApplicationException, BusinessException;

	CustomerTokenUserModel findByAssignedUserCode(String userCode, boolean isThrowError)
			throws ApplicationException, BusinessException;

	void saveCustomerTokenUserAndAssign(String tokenNo, String registeredBy, String customerId, String tokenTypeCode)
			throws ApplicationException, BusinessException;
}
