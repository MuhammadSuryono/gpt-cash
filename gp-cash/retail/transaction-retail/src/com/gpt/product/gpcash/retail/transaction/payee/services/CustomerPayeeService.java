package com.gpt.product.gpcash.retail.transaction.payee.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerPayeeService {

	void checkCustomerPayeeMustExist(String payeeName, String customerId) throws Exception;

	Map<String, Object> searchCustomerPayee(String customerId) throws ApplicationException, BusinessException;

	void checkCustomerPayee(String payeeName, String customerId) throws Exception;

	void saveCustomerPayee(String customerId, String payeeName, String description, String value1,
			String value2, String value3, String value4, String value5, String institutionCode, String createdBy)
			throws Exception;

}