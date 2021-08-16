package com.gpt.product.gpcash.retail.logging.error.services;

import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerErrorLogSC {
	void saveCustomerErrorLog(String id, String referenceNo, String errorTrace, String customerId, String logId);
}
