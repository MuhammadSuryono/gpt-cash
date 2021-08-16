package com.gpt.product.gpcash.corporate.corporate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface CorporateSC extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_CORP";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getContactList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateStatusCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchServicePackageForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchTokenUserForEditAdmin(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;
}
