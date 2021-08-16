package com.gpt.product.gpcash.retail.changepassword.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerChangePasswordSC{
	String menuCode = "MNU_R_GPCASH_F_CHG_PASSWD";
	
	Map<String, Object> userChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getInfo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> changePassword(Map<String, Object> map) throws ApplicationException, BusinessException;
}