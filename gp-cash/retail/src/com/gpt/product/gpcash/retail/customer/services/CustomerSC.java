package com.gpt.product.gpcash.retail.customer.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CustomerSC extends WorkflowService {
	
	String menuCode = "MNU_R_GPCASH_CUST";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStatusCustomer(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchServicePackageForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPostCodeForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchIdentityTypeForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchSubStateForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchStateForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCountryForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCityForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException;
}