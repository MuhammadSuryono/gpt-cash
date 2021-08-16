package com.gpt.product.gpcash.retail.token.validation.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;

@AutoDiscoveryImpl
public interface CustomerTokenValidationService {
	String getChallenge(String userId, String tokenNo)
			throws ApplicationException, BusinessException;

	void authenticate(String userId, String tokenNo, String challengeNo, String responseNo)
			throws ApplicationException, BusinessException;
	
	void blockToken(CustomerTokenUserModel tokenModel) throws Exception;
}
