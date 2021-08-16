package com.gpt.product.gpcash.corporate.transaction.cheque.report.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.IGenerateReport;

@AutoDiscoveryImpl
public interface ChequeOrderReportService extends IGenerateReport {

	Map<String, Object> downloadReport(Map<String, Object> map, String requestBy)
			throws ApplicationException;
}
