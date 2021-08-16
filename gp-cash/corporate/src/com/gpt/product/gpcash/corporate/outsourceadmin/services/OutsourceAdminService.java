package com.gpt.product.gpcash.corporate.outsourceadmin.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface OutsourceAdminService {
	Map<String, Object> saveForLogin(Map<String, Object> map) throws ApplicationException, BusinessException;
	

}
