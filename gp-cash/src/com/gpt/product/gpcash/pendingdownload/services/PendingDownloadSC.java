package com.gpt.product.gpcash.pendingdownload.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface PendingDownloadSC {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	void doGenerateReport(String parameter) throws ApplicationException, BusinessException;
}
