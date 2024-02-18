package com.gpt.product.gpcash.corporate.transaction.report.domtrxreport.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.IGenerateReport;

@AutoDiscoveryImpl
public interface DomesticReportService extends IGenerateReport{
	
	public Map<String, Object> downloadReport(Map<String, Object> map, String requestBy) throws ApplicationException;
	
	public Map<String, Object> getDomServiceList();

}
