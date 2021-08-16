package com.gpt.product.gpcash.corporate.inquiry.sp2d.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface SP2DInquiryService {
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
