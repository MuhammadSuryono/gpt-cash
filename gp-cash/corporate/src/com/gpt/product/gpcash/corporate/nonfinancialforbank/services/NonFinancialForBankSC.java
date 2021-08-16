package com.gpt.product.gpcash.corporate.nonfinancialforbank.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface NonFinancialForBankSC {
	String menuCode = "MNU_GPCASH_BO_RPT_CORP_NON_FIN";

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
