package com.gpt.product.gpcash.retail.login.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class CustomerLoginSCImpl implements CustomerLoginSC {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerLoginService customerLoginService;
	
	@Autowired
	private IDMLoginService idmLoginService;
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "passwd"),
		@Variable(name = "key", required = false) //sengaja dibuat false agar tidak di validate 
	})
	@Override
	public Map<String, Object> customerLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		if(map.get(ApplicationConstants.LOGIN_HANDLES_LOGOUT) != null) {
			logout(map);
		}
		
		//replacing to plainPasswd
		//remark if stress test
		map.put("passwd", idmLoginService.getPlainPasswd((String) map.get("passwd"), (String) map.get("key")));
		//--------------------------
		
		logger.debug("map login : " + map);
		
		return customerLoginService.customerLogin(map);
	}
	
	@SuppressWarnings("unchecked")
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_HISTORY_ID, type = List.class) 
	})
	@Override
	public void logout(Map<String, Object> map) throws ApplicationException, BusinessException {
		idmLoginService.logout((ArrayList<String>) map.get(ApplicationConstants.LOGIN_HISTORY_ID),
				(String) map.get(ApplicationConstants.CUST_ID), (Timestamp) map.get(ApplicationConstants.LOGIN_DATE));
	}
	
}
