package com.gpt.component.maintenance.internationalbank.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface InternationalBankSC extends WorkflowService{

	String menuCode = "MNU_GPCASH_MT_INT_BANK";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCountryForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchInternationalBankOrganizationForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;
}