package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.model.TaxPaymentModel;

@AutoDiscoveryImpl
public interface TaxPaymentService extends CorporateUserWorkflowService {
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeBatchTransaction(TaxPaymentModel inhouse);

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> npwpInquiry(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updateTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
}

