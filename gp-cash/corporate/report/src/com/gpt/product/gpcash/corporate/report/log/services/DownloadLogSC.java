package com.gpt.product.gpcash.corporate.report.log.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface DownloadLogSC {
	String menuCode = "MNU_GPCASH_DOWNLOAD_LOG";
	
	Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException;

}
