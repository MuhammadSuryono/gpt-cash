package com.gpt.product.gpcash.corporate.inquiry.newsinfo.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface NewsInformationSC {
	String menuCode = "MNU_GPCASH_F_NEWS_INFO";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
