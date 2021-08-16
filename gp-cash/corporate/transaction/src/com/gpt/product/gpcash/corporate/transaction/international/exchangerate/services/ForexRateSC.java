package com.gpt.product.gpcash.corporate.transaction.international.exchangerate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface ForexRateSC {
	String menuCode = "MNU_GPCASH_F_EXCHANGE_RATE";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
