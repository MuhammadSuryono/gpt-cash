package com.gpt.product.gpcash.corporate.transaction.domestic.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;
import com.gpt.product.gpcash.corporate.transaction.domestic.model.DomesticTransferModel;

@AutoDiscoveryImpl
public interface DomesticTransferService extends CorporateUserWorkflowService {
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	void executeBatchTransaction(DomesticTransferModel inhouse);

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	void checkTransactionThreshold(BigDecimal equivalentTransactionAmount, String transactionServiceCode) throws BusinessException, ApplicationException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException;

        Map<String, Object> updateTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
        
	void executeOnlineFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeOnlineRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException;
	
	Map<String, Object> updatecheckCOTFlag(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> updatecheckCOTFlagList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateCOT(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveTransactionHoliday(Date holidayDate) throws ApplicationException, BusinessException;
	
	void saveTransactionHolidayModel(DomesticTransferModel domestic);
	
	Map<String, Object> updateHolidayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;
}

