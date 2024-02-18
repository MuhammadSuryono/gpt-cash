package com.gpt.product.gpcash.retail.transaction.purchasepayee.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CustomerPurchasePayeeService {

	void checkPayeeMustExist(String payeeName, String customerId) throws Exception;

	Map<String, Object> searchPayee(String customerId) throws ApplicationException, BusinessException;

	void checkPayee(String payeeName, String customerId) throws Exception;

	void savePayee(String customerId, String payeeName, String description, String value1,
			String value2, String value3, String value4, String value5, String institutionCode, String createdBy)
			throws Exception;

}