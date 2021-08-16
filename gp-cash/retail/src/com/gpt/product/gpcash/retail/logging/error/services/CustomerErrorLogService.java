package com.gpt.product.gpcash.retail.logging.error.services;

import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.logging.error.valueobject.CustomerErrorLogVO;

@AutoDiscoveryImpl
public interface CustomerErrorLogService {
	void saveCustomerErrorLog(CustomerErrorLogVO vo) throws Exception;
}
