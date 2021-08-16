package com.gpt.product.gpcash.corporate.transaction.purchasepayee.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface PurchasePayeeService {

	void checkPayeeMustExist(String payeeName, String userGroupId) throws Exception;

	Map<String, Object> searchPayee(String corporateId, String userCode) throws ApplicationException, BusinessException;

	void checkPayee(String payeeName, String userGroupId) throws Exception;

	void savePayee(String corporateId, String corporateUserGroupId, String payeeName, String description, String value1,
			String value2, String value3, String value4, String value5, String institutionCode, String createdBy)
			throws Exception;

}