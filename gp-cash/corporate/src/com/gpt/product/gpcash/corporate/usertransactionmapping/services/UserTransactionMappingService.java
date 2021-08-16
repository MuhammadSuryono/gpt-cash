package com.gpt.product.gpcash.corporate.usertransactionmapping.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface UserTransactionMappingService {

	Map<String, Object> countTotalCreatedTrx(String userCode) throws ApplicationException, BusinessException;

	Map<String, Object> countTotalExecutedTrx(String userCode) throws ApplicationException, BusinessException;

	void updateCreatedTransactionByUserCode(String userCode) throws ApplicationException, BusinessException;

	void updateExecutedTransactionByUserCode(String userCode) throws ApplicationException, BusinessException;

	void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException;

}
