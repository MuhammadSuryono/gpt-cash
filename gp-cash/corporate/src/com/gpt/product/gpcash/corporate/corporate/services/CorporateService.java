package com.gpt.product.gpcash.corporate.corporate.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;

@AutoDiscoveryImpl
public interface CorporateService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveCorporate(CorporateModel corporate, String createdBy, boolean isNeedFlush) throws ApplicationException, BusinessException;
	
	void updateCorporate(CorporateModel corporate, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteCorporate(CorporateModel corporate, String deletedBy) throws ApplicationException, BusinessException;
	
	Map<String, Object> getContactList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findMenuForPendingTask(String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporates() throws ApplicationException, BusinessException;

	Map<String, Object> findNonFinancialMenu(String corporateId, String nonFinMenuType) throws ApplicationException;

	List<String> findListOfStringNonFinancialMenu(String corporateId, String nonFinMenuType)
			throws ApplicationException;
	
	CorporateModel getExistingRecord(String code, boolean isThrowError) throws BusinessException;
}