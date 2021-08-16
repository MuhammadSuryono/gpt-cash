package com.gpt.product.gpcash.corporate.corporateaccount.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;

@AutoDiscoveryImpl
public interface CorporateAccountService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCorporateIdAndAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByCIF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<CorporateAccountModel> searchByCorporateId(String corporateId) throws ApplicationException, BusinessException;

	List<CorporateAccountModel> findCASAAccountByCorporate(String corporateId) throws ApplicationException, BusinessException;
	
	List<CorporateAccountModel> findCAAccountForVAByCorporate(String corporateId) throws ApplicationException, BusinessException;
}
