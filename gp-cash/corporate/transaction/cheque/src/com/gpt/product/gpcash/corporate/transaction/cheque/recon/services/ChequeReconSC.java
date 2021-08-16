package com.gpt.product.gpcash.corporate.transaction.cheque.recon.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface ChequeReconSC {
	String menuCode = "MNU_GPCASH_F_CHQ_RECON";

	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findDetailByOrderNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> findOnlineBySerialNo(Map<String, Object> map) throws ApplicationException, BusinessException;
}
