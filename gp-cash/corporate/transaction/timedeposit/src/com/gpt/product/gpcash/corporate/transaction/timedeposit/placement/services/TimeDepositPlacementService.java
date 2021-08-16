package com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.model.TimeDepositPlacementModel;

@AutoDiscoveryImpl
public interface TimeDepositPlacementService extends CorporateUserWorkflowService {
	
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
}