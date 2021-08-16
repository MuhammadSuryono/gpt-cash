package com.gpt.product.gpcash.retail.transaction.globaltransaction.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;

@AutoDiscoveryImpl
public interface CustomerGlobalTransactionService {

	void updateCreatedTransactionByUserCode(String userCode) throws ApplicationException;

	void updateExecutedTransactionByUserCode(String userCode) throws ApplicationException;

	List<Map<String, Object>> prepareDetailTransactionMapFromPendingTask(CustomerUserPendingTaskModel model,
			CustomerTransactionStatusModel trxStatus) throws Exception;

	Map<String, Object> checkBalance(String customerId, String accountDtlId)
			throws ApplicationException, BusinessException;

	BigDecimal checkBalance(AccountModel account) throws ApplicationException, BusinessException;

	void save(String customerId, String menuCode, String serviceCode, String referenceNo, String modelId,
			String transactionCurrency, BigDecimal totalChargeEquivalent, BigDecimal totalTransactionAmountEquivalent,
			BigDecimal totalDebitedAmountEquivalent, String isError) throws ApplicationException, BusinessException;

}
