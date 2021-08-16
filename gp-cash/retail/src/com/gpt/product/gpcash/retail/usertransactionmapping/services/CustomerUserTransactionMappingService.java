package com.gpt.product.gpcash.retail.usertransactionmapping.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerUserTransactionMappingService {

	Map<String, Object> countTotalCreatedTrx(String customerId) throws ApplicationException, BusinessException;

	Map<String, Object> countTotalExecutedTrx(String customerId) throws ApplicationException, BusinessException;

	void updateCreatedTransactionByUserCode(String customerId) throws ApplicationException, BusinessException;

	void updateExecutedTransactionByUserCode(String customerId) throws ApplicationException, BusinessException;

	void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException;

}
