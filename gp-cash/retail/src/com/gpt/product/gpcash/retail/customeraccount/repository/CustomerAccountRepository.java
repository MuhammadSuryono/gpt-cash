package com.gpt.product.gpcash.retail.customeraccount.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccountModel, String>, CashRepository<CustomerAccountModel>{

	Page<CustomerAccountModel> findByCustomerId(String customerId, Pageable pageInfo) throws Exception;
	
	@Query("select distinct custAccount.customer from CustomerAccountModel custAccount "
			+ "where custAccount.customer.userId like ?1 "
			+ "and custAccount.account.accountNo like ?2 "
			+ "and custAccount.customer.name like ?3 ")
	Page<CustomerModel> findByUserIdAndAccountNoAndName(String userId, String accountNo, String name, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountNo = ?2")
	CustomerAccountModel findByCustomerIdAndAccountNo(String customerId, String accountNo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "order by custAccount.account.accountNo ")
	CustomerAccountModel findByCustomerId(String customerId) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.id = ?2")
	CustomerAccountModel findByCustomerIdAndAccountId(String customerId, String accountId) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountNo like ?2")
	Page<CustomerAccountModel> findByCustomerIdAndAccountNoLike(String customerId, String accountNo, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2)")
	Page<CustomerAccountModel> findCASAAccountByCustomer(String customerId, List<String> accountTypeList, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2) "
			+ "and custAccount.isInquiry = 'Y' ")
	Page<CustomerAccountModel> findCASAAccountByCustomerForInquiryOnly(String customerId, List<String> accountTypeList, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2) "
			+ "and custAccount.account.branch.code = ?3 ")
	Page<CustomerAccountModel> findCASAAccountByCustomerAndBranchCode(String customerId, List<String> accountTypeList, String branchCode, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2)"
			+ "and custAccount.isDebit = 'Y'"
			+ "and custAccount.account.currency.code = ?3 "
			+ "order by custAccount.account.accountNo")
	Page<CustomerAccountModel> findByCustomerIdAndIsDebit(String customerId, List<String> accountTypeList, String localCurrency, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2)"
			+ "and custAccount.isCredit = 'Y'"
			+ "and custAccount.account.currency.code = ?3 "
			+ "order by custAccount.account.accountNo")
	Page<CustomerAccountModel> findByCustomerIdAndIsCredit(String customerId, List<String> accountTypeList, String localCurrency, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2)"
			+ "and custAccount.isDebit = 'Y'"
			+ "order by custAccount.account.accountNo")
	Page<CustomerAccountModel> findByCustomerIdAndIsDebitMultiCurrency(String customerId, List<String> accountTypeList, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.account.accountType.code in (?2)"
			+ "and custAccount.isCredit = 'Y'"
			+ "order by custAccount.account.accountNo")
	Page<CustomerAccountModel> findByCustomerIdAndIsCreditMultiCurrency(String customerId, List<String> accountTypeList, Pageable pageInfo) throws Exception;
	
	@Query("select count(*) as count from CustomerAccountModel custAccount "
			+ "where custAccount.customer.id = ?1 ")
	Object getCountByCustomerId(String customerId) throws Exception;
}