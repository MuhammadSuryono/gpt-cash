package com.gpt.product.gpcash.retail.inquiry.accountstatement.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerAccountStatementService {
	
	Map<String, Object> getPeriods(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> download(Map<String, Object> map) throws ApplicationException, BusinessException;

}
