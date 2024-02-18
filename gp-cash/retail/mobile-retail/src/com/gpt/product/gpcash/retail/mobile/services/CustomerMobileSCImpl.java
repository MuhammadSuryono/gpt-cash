package com.gpt.product.gpcash.retail.mobile.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.platform.cash.constants.ApplicationConstants;

/**
 * TODO: need to record into activity log
 * 
 *
 */

@Validate
@Service
public class CustomerMobileSCImpl implements CustomerMobileSC {

	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private CustomerMobileService mobileService;	
	
	@SuppressWarnings("unchecked")
	private void handleLogoutIfNeeded(Map<String, Object> map) throws BusinessException, ApplicationException {
		if(map.get(ApplicationConstants.LOGIN_HANDLES_LOGOUT) != null) {
			idmLoginService.logout((List<String>) map.get(ApplicationConstants.LOGIN_HISTORY_ID),
					(String) map.get(ApplicationConstants.CUST_ID), (Timestamp) map.get(ApplicationConstants.LOGIN_DATE));
		}
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "passwd"),
		@Variable(name = "key"), 
		@Variable(name = "device", required = false, type = Map.class), //sengaja dibuat false agar tidak di validate 
	})
	@Override
	public Map<String, Object> authenticate(Map<String, Object> map) throws ApplicationException, BusinessException {
		handleLogoutIfNeeded(map);
		return mobileService.authenticate(map);
	}	
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = "device", required = false, type = Map.class) 
	})
	@Override
	public void finalizeSetup(Map<String, Object> map) throws ApplicationException, BusinessException {
		mobileService.finalizeSetup(map);
	}

	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = "key"),    		// the heartBeat
		@Variable(name = "pk"), 			// base64 of encrypted public key
		@Variable(name = "device", required = false, type = Map.class) //sengaja dibuat false agar tidak di validate 
	})
	@Override
	public void registerFP(Map<String, Object> map) throws ApplicationException, BusinessException {
		mobileService.registerFP(map);
	}
	
	@Input({ 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = "device", required = false, type = Map.class), //sengaja dibuat false agar tidak di validate 
	})
	@Override
	public void activateDevice(Map<String, Object> map) throws ApplicationException, BusinessException {
		mobileService.activateDevice(map);
	}

	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "passwd"),  // the signature of the heartBeat
		@Variable(name = "key"),   // the heartBeat
		@Variable(name = "device", required = false, type = Map.class)  //sengaja dibuat false agar tidak di validate
	})
	@Override
	public Map<String, Object> mobileCustLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		handleLogoutIfNeeded(map);
		return mobileService.mobileCustLogin(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "nonce", required = false),  // encrypted with heartBeat
		@Variable(name = "sign"),  // the signature of the nonce
		@Variable(name = "device", required = false, type = Map.class)  //sengaja dibuat false agar tidak di validate
	})
	@Override
	public Map<String, Object> mobileCustFPLogin(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		handleLogoutIfNeeded(map);
		return mobileService.mobileCustFPLogin(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "oldPassword"),
		@Variable(name = "newPassword"),
		@Variable(name = "newPassword2"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"RESET"}),
		@Variable(name = "key", required = false) //sengaja dibuat false agar tidak di validate 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> mobileForceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		return mobileService.mobileForceChangePassword(map);
	}
}
