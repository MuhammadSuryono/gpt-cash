package com.gpt.product.gpcash.corporate.corporatelimitusage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateLimitUsageSC {
	
	String menuCode = "MNU_GPCASH_F_TRX_LMT_USG";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

}
