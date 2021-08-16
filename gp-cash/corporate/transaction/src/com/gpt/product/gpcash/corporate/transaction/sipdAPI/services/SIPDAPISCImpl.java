package com.gpt.product.gpcash.corporate.transaction.sipdAPI.services;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class SIPDAPISCImpl implements SIPDAPISC {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SIPDAPIService spidAPIService;
	
	
	@Validate
	@Input({ 
		@Variable(name = "client_id"),
		@Variable(name = "client_secret")
	})
	public Map<String, Object> getTokenAPI(Map<String, Object> map) throws ApplicationException, BusinessException {
	
		return spidAPIService.getTokenAPI(map);
	}

	@Validate
	@Input({ 
		@Variable(name = "amount", type = BigDecimal.class),
	})
	@Override
	public Map<String, Object> postDataSIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		//spidAPIService.validateToken(map);
		
		return spidAPIService.postDataSIPD(map);
	}

	@Override
	public Map<String, Object> inquirySIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return spidAPIService.inquirySIPD(map);
	}

	@Override
	public Map<String, Object> checkStatusSIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return spidAPIService.checkStatusSIPD(map);
	}
	
	
	
}
