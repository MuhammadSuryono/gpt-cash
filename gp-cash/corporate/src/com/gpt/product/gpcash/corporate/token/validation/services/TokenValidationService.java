package com.gpt.product.gpcash.corporate.token.validation.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;

@AutoDiscoveryImpl
public interface TokenValidationService {
	String getChallenge(String corporateId, String userCode, String tokenNo)
			throws ApplicationException, BusinessException;

	void authenticate(String corporateId, String userCode, String tokenNo, String challengeNo, String responseNo)
			throws ApplicationException, BusinessException;
	
	void blockToken(TokenUserModel tokenModel) throws Exception;
}
