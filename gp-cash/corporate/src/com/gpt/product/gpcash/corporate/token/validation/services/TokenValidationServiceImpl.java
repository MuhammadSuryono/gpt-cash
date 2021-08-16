package com.gpt.product.gpcash.corporate.token.validation.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.repository.TokenUserRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TokenValidationServiceImpl implements TokenValidationService{
	@Autowired
	private TokenUserRepository tokenUserRepo;
	
	@Autowired
	private TokenValidationService self;	

	@Autowired
	private EAIEngine eaiAdapter;	
	
	@Override
	public String getChallenge(String corporateId, String userCode, String tokenNo) throws ApplicationException, BusinessException {
		try{
			TokenUserModel model = isTokenForTrxValid(corporateId, tokenNo, userCode);
			
			Map<String, Object>  inputs = new HashMap<>();
			inputs.put("tokenType", model.getTokenType().getCode());
			inputs.put("tokenNo", tokenNo);
			
			return (String)eaiAdapter.invokeService(EAIConstants.GENERATE_TOKEN_CHALLENGE_N0, inputs).get("challengeNo");
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void authenticate(String corporateId, String userCode, String tokenNo, 
			String challengeNo, String responseNo) throws ApplicationException, BusinessException {
		TokenUserModel model = null;
		try{
			model = isTokenForTrxValid(corporateId, tokenNo, userCode);
			
			Map<String, Object>  inputs = new HashMap<>();
			inputs.put("tokenType", model.getTokenType().getCode());
			inputs.put("corpId", corporateId);
			inputs.put("userId", userCode);
			inputs.put("tokenNo", tokenNo);
			inputs.put("challengeNo", challengeNo);
			inputs.put("responseNo", responseNo);
			
//			eaiAdapter.invokeService(EAIConstants.TOKEN_AUTHENTICATION, inputs);
			
		} catch (BusinessException be) {
			if (EAIConstants.ERROR_TOKEN_BLOCKED.equals(be.getErrorCode())) {
				try {
					self.blockToken(model);
				} catch (Exception e) {
					throw new ApplicationException(e);
				}
			}
			
			throw be;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void blockToken(TokenUserModel tokenModel) throws Exception {
		tokenModel.setStatus(ApplicationConstants.TOKEN_STATUS_BLOCKED);
		
		tokenUserRepo.save(tokenModel);
	}
	
	
	private TokenUserModel isTokenForTrxValid(String corporateId, String tokenNo, String userCode) throws Exception{
		TokenUserModel model = (TokenUserModel) tokenUserRepo.searchByTokenNoActive(corporateId, tokenNo, userCode);

		if (model == null)
			throw new BusinessException("GPT-0100123");
		
		return model;
	}
}
