package com.gpt.product.gpcash.corporate.transaction.international.underlyingdoc.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface UnderlyingDocumentService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void validateDetail(String docTypeCode, String underlyingAmount) throws ApplicationException, BusinessException;
	
}
