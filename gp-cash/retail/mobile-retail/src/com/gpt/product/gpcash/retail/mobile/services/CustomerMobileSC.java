package com.gpt.product.gpcash.retail.mobile.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerMobileSC {
	
	Map<String, Object> authenticate(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void finalizeSetup(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void registerFP(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> mobileCustLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> mobileCustFPLogin(Map<String, Object> map) throws ApplicationException, BusinessException;

	void activateDevice(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> mobileForceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;

}
