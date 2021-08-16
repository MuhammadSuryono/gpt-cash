package com.gpt.product.gpcash.retail.login.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerLoginService {

	Map<String, Object> customerLogin(Map<String, Object> map) throws ApplicationException, BusinessException;

}
