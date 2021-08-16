package com.gpt.product.gpcash.corporate.transaction.report.charging.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface ChargingReportSC {
	String menuCode = "MNU_GPCASH_BO_RPT_CHG";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException;

	void doGenerateReport(Map<String, Object> map) throws ApplicationException, BusinessException;
}