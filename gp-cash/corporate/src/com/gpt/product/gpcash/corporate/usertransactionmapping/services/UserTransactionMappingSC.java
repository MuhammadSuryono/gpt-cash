package com.gpt.product.gpcash.corporate.usertransactionmapping.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface UserTransactionMappingSC {

	void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException;

}
