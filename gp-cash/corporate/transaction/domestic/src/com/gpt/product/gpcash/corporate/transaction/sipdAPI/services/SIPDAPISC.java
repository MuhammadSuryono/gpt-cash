package com.gpt.product.gpcash.corporate.transaction.sipdAPI.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface SIPDAPISC {

	Map<String, Object> getTokenAPI(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> postDataSIPD(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> inquirySIPD(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> checkStatusSIPD(Map<String, Object> map) throws ApplicationException, BusinessException;

}
