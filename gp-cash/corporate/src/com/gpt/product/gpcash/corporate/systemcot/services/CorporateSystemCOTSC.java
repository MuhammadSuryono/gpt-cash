package com.gpt.product.gpcash.corporate.systemcot.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateSystemCOTSC{
	
	String menuCode = "MNU_GPCASH_F_COT";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
}
