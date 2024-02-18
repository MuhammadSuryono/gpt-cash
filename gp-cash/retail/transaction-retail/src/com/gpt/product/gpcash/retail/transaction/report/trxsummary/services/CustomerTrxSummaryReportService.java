package com.gpt.product.gpcash.retail.transaction.report.trxsummary.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.IGenerateReport;

@AutoDiscoveryImpl
public interface CustomerTrxSummaryReportService extends IGenerateReport {

	Map<String, Object> downloadReport(Map<String, Object> map, String requestBy)
			throws ApplicationException;
}
