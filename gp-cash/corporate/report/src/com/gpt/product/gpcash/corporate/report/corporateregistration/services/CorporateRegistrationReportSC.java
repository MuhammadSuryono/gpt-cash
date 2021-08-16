package com.gpt.product.gpcash.corporate.report.corporateregistration.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateRegistrationReportSC {
	String menuCode = "MNU_GPCASH_BO_RPT_CORP_REGIS";
	
	Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	void doGenerateReport(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
