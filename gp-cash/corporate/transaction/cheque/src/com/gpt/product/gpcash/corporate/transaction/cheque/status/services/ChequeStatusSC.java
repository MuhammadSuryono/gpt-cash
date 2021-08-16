package com.gpt.product.gpcash.corporate.transaction.cheque.status.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface ChequeStatusSC {
	String menuCode = "MNU_GPCASH_CHQ_STS";

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitProcess(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> handover(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchIdentityTypeForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
}
