package com.gpt.product.gpcash.retail.transaction.domestic.services;

import java.math.BigDecimal;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;
import com.gpt.product.gpcash.retail.transaction.domestic.model.CustomerDomesticTransferModel;

@AutoDiscoveryImpl
public interface CustomerDomesticTransferService extends CustomerUserWorkflowService {
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(CustomerDomesticTransferModel domestic);

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	void checkTransactionThreshold(BigDecimal equivalentTransactionAmount, String transactionServiceCode) throws BusinessException, ApplicationException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void executeOnlineFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeOnlineRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
}

