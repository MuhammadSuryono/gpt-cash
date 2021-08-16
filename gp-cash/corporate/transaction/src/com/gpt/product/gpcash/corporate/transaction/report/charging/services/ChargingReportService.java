package com.gpt.product.gpcash.corporate.transaction.report.charging.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.IGenerateReport;

@AutoDiscoveryImpl
public interface ChargingReportService extends IGenerateReport {

	Map<String, Object> downloadReport(Map<String, Object> map, String requestBy)
			throws ApplicationException;
	
	Map<String, Object> searchMenu() throws ApplicationException;
}
