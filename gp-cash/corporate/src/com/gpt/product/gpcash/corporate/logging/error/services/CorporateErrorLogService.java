package com.gpt.product.gpcash.corporate.logging.error.services;

import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.logging.error.valueobject.CorporateErrorLogVO;

@AutoDiscoveryImpl
public interface CorporateErrorLogService {
	void saveCorporateErrorLog(CorporateErrorLogVO vo) throws Exception;
}
