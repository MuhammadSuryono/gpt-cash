package com.gpt.product.gpcash.corporate.transaction.international.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.international.model.InternationalTransferModel;

@AutoDiscoveryImpl
public interface InternationalTransferService extends CorporateUserWorkflowService {
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(InternationalTransferModel inhouse);

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> searchForBank(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException;

}

