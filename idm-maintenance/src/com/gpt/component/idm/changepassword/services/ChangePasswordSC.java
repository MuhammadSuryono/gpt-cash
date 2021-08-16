package com.gpt.component.idm.changepassword.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface ChangePasswordSC{
	String menuCode = "MNU_GPCASH_IDM_CHG_PASSWD";
	
	Map<String, Object> userChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getInfo(Map<String, Object> map) throws ApplicationException, BusinessException;
}