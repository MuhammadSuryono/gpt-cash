package com.gpt.product.gpcash.cot.currency.services;

import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CurrencyCOTService {
	void validateSystemCOT(String code, String applicationCode) throws Exception;

	void validateSystemCOT(String code, String applicationCode, String sessionTime) throws Exception;
}
