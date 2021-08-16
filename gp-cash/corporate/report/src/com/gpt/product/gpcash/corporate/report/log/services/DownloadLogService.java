package com.gpt.product.gpcash.corporate.report.log.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.IGenerateReport;

@AutoDiscoveryImpl
public interface DownloadLogService {

	Map<String, Object> downloadReport(Map<String, Object> map, String requestBy)
			throws ApplicationException;
}
