package com.gpt.product.gpcash.corporate.logging.error.services;

import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateErrorLogSC {
	void saveCorporateErrorLog(String id, String referenceNo, String errorTrace, String corporateId, String logId);
}
