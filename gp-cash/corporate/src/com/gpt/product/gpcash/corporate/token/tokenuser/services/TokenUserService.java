package com.gpt.product.gpcash.corporate.token.tokenuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;

@AutoDiscoveryImpl
public interface TokenUserService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	TokenUserModel saveTokenUser(String tokenNo, String registeredBy, String corporateId, String tokenTypeCode)	throws ApplicationException, BusinessException;

	void deleteTokenUser(String tokenId) throws ApplicationException, BusinessException;

	void saveTokenUserAndAssign(String tokenNo, String registeredBy, String assignedUserStr, String corporateId, String tokenTypeCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	void unassignToken(String userCode, String tokenNo, String corporateId)
			throws ApplicationException, BusinessException;

	Map<String, Object> unblockToken(String userCode, String tokenNo, String corporateId)
			throws ApplicationException, BusinessException;

	Map<String, Object> unlockToken(String userCode, String tokenNo, String corporateId, String randomLockedCode)
			throws ApplicationException, BusinessException;

	void assignToken(String userCode, String tokenNo, String corporateId, String createdBy)
			throws ApplicationException, BusinessException;

	TokenUserModel findByAssignedUserCode(String userCode, boolean isThrowError)
			throws ApplicationException, BusinessException;
}
