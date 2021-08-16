package com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface TimeDepositPlacementSC extends CorporateUserWorkflowService {
	String menuCode = "MNU_GPCASH_F_TD_PLACEMENT";
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> checkBalance(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchProductsForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTermByProductsForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
}
