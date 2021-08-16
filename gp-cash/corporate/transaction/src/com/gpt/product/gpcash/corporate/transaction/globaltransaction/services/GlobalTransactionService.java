package com.gpt.product.gpcash.corporate.transaction.globaltransaction.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;

@AutoDiscoveryImpl
public interface GlobalTransactionService {

	void updateCreatedTransactionByUserCode(String userCode) throws ApplicationException;

	void updateExecutedTransactionByUserCode(String userCode) throws ApplicationException;

	List<Map<String, Object>> prepareDetailTransactionMapFromPendingTask(CorporateUserPendingTaskModel model,
			TransactionStatusModel trxStatus) throws Exception;

	Map<String, Object> checkBalance(String corporateId, String userCode, String accountGroupDtlId)
			throws ApplicationException, BusinessException;

	BigDecimal checkBalance(AccountModel account) throws ApplicationException, BusinessException;

	void save(String corporateId, String menuCode, String serviceCode, String referenceNo, String modelId,
			String transactionCurrency, BigDecimal totalChargeEquivalent, BigDecimal totalTransactionAmountEquivalent,
			BigDecimal totalDebitedAmountEquivalent, String isError) throws ApplicationException, BusinessException;

}
