package com.gpt.product.gpcash.corporate.transaction.international.status.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface InternationalTransferStatusSC {
	String menuCode = "MNU_GPCASH_INT_STS";
	
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	
}
