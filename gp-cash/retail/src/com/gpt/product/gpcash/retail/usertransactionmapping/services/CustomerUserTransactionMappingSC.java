package com.gpt.product.gpcash.retail.usertransactionmapping.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerUserTransactionMappingSC {

	void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException;

}
