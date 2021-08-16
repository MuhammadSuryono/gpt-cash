package com.gpt.product.gpcash.corporate.inquiry.sp2d.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface SP2DInquirySC {
	
	String menuCode = "MNU_GPCASH_F_SP2D_INQ";

	Map<String, Object> searchSP2DOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPemdaCodeForDroplist(Map<String, Object> map)	throws ApplicationException, BusinessException;
}
