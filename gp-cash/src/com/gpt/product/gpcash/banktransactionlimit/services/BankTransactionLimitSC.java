package com.gpt.product.gpcash.banktransactionlimit.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface BankTransactionLimitSC extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_BANK_TRX_LMT";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
