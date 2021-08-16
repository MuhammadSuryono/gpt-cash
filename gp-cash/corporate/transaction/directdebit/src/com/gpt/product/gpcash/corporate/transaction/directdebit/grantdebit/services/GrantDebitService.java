package com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.model.GrantDebitModel;

@AutoDiscoveryImpl
public interface GrantDebitService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<GrantDebitModel> searchByCorporateId(String corporateId) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCorporateIdAndAccount(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> validateDetail(String accountNo, Date expiryDate) throws ApplicationException, BusinessException;
}
