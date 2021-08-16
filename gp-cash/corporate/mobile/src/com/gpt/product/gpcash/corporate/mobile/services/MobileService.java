package com.gpt.product.gpcash.corporate.mobile.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface MobileService {

	Map<String, Object> authenticate(Map<String, Object> map) throws ApplicationException, BusinessException;

	void finalizeSetup(Map<String, Object> map) throws ApplicationException, BusinessException;

	void registerFP(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> mobileCorpLogin(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> mobileCorpFPLogin(Map<String, Object> map) throws ApplicationException, BusinessException;

	void activateDevice(Map<String, Object> map) throws ApplicationException, BusinessException;
}
