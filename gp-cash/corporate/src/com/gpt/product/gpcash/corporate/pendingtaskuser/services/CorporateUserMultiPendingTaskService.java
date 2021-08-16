package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateUserMultiPendingTaskService {
	Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException;
}
