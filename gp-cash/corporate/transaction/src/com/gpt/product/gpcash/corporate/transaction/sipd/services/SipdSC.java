package com.gpt.product.gpcash.corporate.transaction.sipd.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface SipdSC extends CorporateUserWorkflowService{
	String menuCode = "MNU_GPCASH_F_PEMDA_SIPD";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchOwnAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> searchBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException;
	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
}
