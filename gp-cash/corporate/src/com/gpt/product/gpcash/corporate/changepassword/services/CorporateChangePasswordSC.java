package com.gpt.product.gpcash.corporate.changepassword.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateChangePasswordSC{
	String menuCode = "MNU_GPCASH_F_CHG_PASSWD";
	
	/**
	 * @deprecated
	 * @param map
	 * @return
	 * @throws ApplicationException
	 * @throws BusinessException
	 * 
	 * use @link {@link #changePassword(Map)} instead
	 */
	@Deprecated
	Map<String, Object> userChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> changePassword(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getInfo(Map<String, Object> map) throws ApplicationException, BusinessException;
}