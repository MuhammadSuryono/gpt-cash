package com.gpt.component.maintenance.sysparam.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface SysParamService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<String> getTransactionSessionTime() throws ApplicationException, BusinessException;

	String getProductName() throws ApplicationException, BusinessException;

	String getValueByCode(String code) throws BusinessException, ApplicationException;

	String getLocalCurrency() throws ApplicationException, BusinessException;

}
