package com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerUnderlyingDocumentSC {
	String menuCode = "MNU_R_GPCASH_MT_UNDERLYING_DOC";
	
	Map<String, Object> searchCustomer(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateDetail(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchDocumentTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	
}
