package com.gpt.product.gpcash.retail.customeraccount.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;

@AutoDiscoveryImpl
public interface CustomerAccountService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCustomerIdAndAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByCIF(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineByAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<CustomerAccountModel> searchByCustomerId(String customerId) throws ApplicationException, BusinessException;

	List<CustomerAccountModel> findCASAAccountByCustomer(String customerId)
			throws ApplicationException, BusinessException;

	void saveCustomerAccount(String cifId, String accountNo, String accountBranchCode, String accountCurrencyCode,
			String accountTypeCode, String accountName, String accountAlias, String isDebit, String isCredit,
			String isInquiry, String customerId, String createdBy) throws ApplicationException, BusinessException;

	Map<String, Object> findByCustomerIdAndIsDebit(String customerId)
			throws ApplicationException, BusinessException;

	Map<String, Object> findCASAAccountByCustomerGetMap(String customerId)
			throws ApplicationException, BusinessException;

	List<CustomerAccountModel> findCASAAccountByCustomerAndBranch(String customerId, String branchCode)
			throws ApplicationException, BusinessException;

	List<CustomerAccountModel> findCASAAccountByCustomerAndAccountType(String customerId, List<String> casaAccountType)
			throws ApplicationException, BusinessException;

	List<CustomerAccountModel> findCASAAccountByCustomerAndAccountTypeForInquiryOnly(String customerId,
			List<String> casaAccountType) throws ApplicationException, BusinessException;

	Map<String, Object> findCASAAccountByCustomerAndAccountTypeForInquiryOnlyGetMap(String customerId,
			List<String> casaAccountType) throws ApplicationException, BusinessException;
	
	Map<String, Object> findCASAAccountByCustomerAndVirtualAccountTypeForInquiryOnlyGetMap(String customerId,
			List<String> casaAccountType) throws ApplicationException, BusinessException;

	Map<String, Object> findByCustomerIdAndIsCredit(String customerId) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCustomerId(Map<String, Object> map) throws ApplicationException, BusinessException;

	int getCountCustomerAccount(String customerId) throws ApplicationException, BusinessException;
	

	Map<String, Object> findByCustomerIdAndIsDebitMultiCurrency(String customerId)
			throws ApplicationException, BusinessException;
	
	
	Map<String, Object> findByCustomerIdAndIsCreditMultiCurrency(String customerId) throws ApplicationException, BusinessException;
}
