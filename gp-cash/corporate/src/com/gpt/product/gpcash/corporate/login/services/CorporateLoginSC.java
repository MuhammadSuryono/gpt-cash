package com.gpt.product.gpcash.corporate.login.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateLoginSC {

	Map<String, Object> corporateLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> corporateLoginOutsource(Map<String, Object> map) throws ApplicationException, BusinessException;

	void logout(Map<String, Object> map) throws ApplicationException, BusinessException;

}
