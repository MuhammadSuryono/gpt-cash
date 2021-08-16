package com.gpt.product.gpcash.corporate.pendingtaskadmin.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateAdminMultiPendingTaskService {
	Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException;
}
