package com.gpt.product.gpcash.corporate.transaction.tax.ntpninquiry.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface NTPNInquirySC{
	String menuCode = "MNU_GPCASH_TAX_NTPN_INQ";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> NTPNInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> NTPNInquiryAll(Map<String, Object> map) throws ApplicationException, BusinessException;
}