package com.gpt.product.gpcash.corporate.inquiry.accountstatement.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface AccountStatementSC {
	
	String menuCode = "MNU_GPCASH_F_ACCT_STATEMENT";
	
	Map<String, Object> getPeriods(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccountGroupForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;
		
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> download(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
