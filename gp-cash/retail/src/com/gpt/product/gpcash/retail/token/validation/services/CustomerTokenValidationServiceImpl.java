package com.gpt.product.gpcash.retail.token.validation.services;

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
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;
import com.gpt.product.gpcash.retail.token.tokenuser.repository.CustomerTokenUserRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTokenValidationServiceImpl implements CustomerTokenValidationService{
	@Autowired
	private CustomerTokenUserRepository tokenUserRepo;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private CustomerTokenValidationService self;	

	@Autowired
	private EAIEngine eaiAdapter;	
	
	@Override
	public String getChallenge(String userId, String tokenNo) throws ApplicationException, BusinessException {
		try{
			CustomerTokenUserModel model = isTokenForTrxValid(userId, tokenNo);
			
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
	public void authenticate(String userId, String tokenNo, 
			String challengeNo, String responseNo) throws ApplicationException, BusinessException {
		CustomerTokenUserModel model = null;
		try{
			model = isTokenForTrxValid(userId, tokenNo);
			
			Map<String, Object>  inputs = new HashMap<>();
			inputs.put("tokenType", model.getTokenType().getCode());
			inputs.put("userId", userId);
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
	public void blockToken(CustomerTokenUserModel tokenModel) throws Exception {
		tokenModel.setStatus(ApplicationConstants.TOKEN_STATUS_BLOCKED);
		
		tokenUserRepo.save(tokenModel);
	}
	
	
	private CustomerTokenUserModel isTokenForTrxValid(String customerId, String tokenNo) throws Exception{
		CustomerModel user = customerRepo.findOne(customerId);
		
		boolean isTokenValid = false;
		
		CustomerTokenUserModel model = null;
		if(user != null) {
			model = (CustomerTokenUserModel) tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, user.getId());
			
			if(model != null)
				isTokenValid = true;
			
		}

		if (!isTokenValid)
			throw new BusinessException("GPT-0100123");
		
		return model;
	}
}
