package com.gpt.product.gpcash.retail.customerlimit.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.customerlimit.model.CustomerLimitModel;

@Repository
public interface CustomerLimitRepository extends JpaRepository<CustomerLimitModel, String>, CashRepository<CustomerLimitModel> {
	@Query("select customerLimit.id, customerLimit.service.code, service.name, customerLimit.maxAmountLimit, customerLimit.maxOccurrenceLimit, "
			 + "customerLimit.amountLimitUsage, customerLimit.occurrenceLimitUsage, customerLimit.currency.code, "
			 + "currency.name, currencyMatrix.code, currencyMatrix.name from CustomerLimitModel customerLimit "
		     + "left join customerLimit.service service "
		     + "left join customerLimit.currency currency "
		     + "left join customerLimit.serviceCurrencyMatrix serviceCurrencyMatrix "
			 + "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix "
			 + "where customerLimit.application.code = (?1) and service.serviceType.code = (?2) and customerLimit.customer.id = (?3) and service.deleteFlag = 'N' order by service.idx")
	Page<Object[]> findLimit(String applicationCode, String serviceTypeCode, String customerId, Pageable pageInfo) throws Exception;
		
	@Query("select customerLimit.id, customerLimit.service.code, service.name, customerLimit.maxAmountLimit, customerLimit.maxOccurrenceLimit, "
			+ "customerLimit.amountLimitUsage, customerLimit.occurrenceLimitUsage, customerLimit.currency.code, "
			+ "currency.name, currencyMatrix.code, currencyMatrix.name from CustomerLimitModel customerLimit "
			+ "left join customerLimit.service service "
			+ "left join customerLimit.currency currency "
			+ "left join customerLimit.serviceCurrencyMatrix serviceCurrencyMatrix "
			+ "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix "
			+ "where customerLimit.application.code = (?1) and customerLimit.id in (?2) and service.deleteFlag = 'N' order by service.idx")
	Page<Object[]> findLimit(String applicationCode, List<String> customerLimitId, Pageable pageInfo) throws Exception;
	
	@Query("from CustomerLimitModel a "
			 + "where a.application.code = ?1 "
			 + "and a.service.code = ?2 "
			 + "and a.serviceCurrencyMatrix.currencyMatrix.code = ?3 "
			 + "and a.currency.code = ?4 "
			 + "and a.customer.id = ?5 "
			 + "and a.service.deleteFlag = 'N'")
	CustomerLimitModel findByServiceAndCurrencyMatrix(String applicationCode, String serviceCode, String currencyMatrixCode, 
			String currencyCode, String customerId) throws Exception;
	
	@Modifying
	@Query("update CustomerLimitModel set amountLimitUsage = 0, occurrenceLimitUsage = 0")
	void resetLimit();
	
	@Modifying
	@Query("update CustomerLimitModel set amountLimitUsage = amountLimitUsage + ?1, occurrenceLimitUsage = occurrenceLimitUsage + 1 "
			+ "where (amountLimitUsage + ?1) <= maxAmountLimit and occurrenceLimitUsage <= maxOccurrenceLimit "
			+ "and id =?2 ")
	void updateCustomerLimit(BigDecimal transactionAmount, String id);
	
	@Modifying
	@Query("update CustomerLimitModel set amountLimitUsage = amountLimitUsage - ?1, occurrenceLimitUsage = occurrenceLimitUsage - 1 " 
			+ "where id =?2 ")
	void reverseUpdateCustomerLimit(BigDecimal transactionAmount, String id);
	
	@Modifying
	@Query("update CustomerLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?4 "
			+ "where limitPackage.code = ?3 "
			+ "and service.code =?5 "
			+ "and serviceCurrencyMatrix.id = ?6 "
			+ "and customer.id in "
				+ "(select a.id from CustomerModel a "
				+ "where a.specialLimitFlag = ?7)")
	void updateLimitsByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId,
			String isUpdateCustomerWithSpecialLimit);
	
	@Modifying
	@Query("update CustomerLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3 "
			+ " where serviceCurrencyMatrix.id = ?4 "
			+ "and customer.id in (select a.id from CustomerModel a where a.specialChargeFlag = ?5)")
	void updateAllLimits(BigDecimal maxAmountLimit, int maxOccurrenceLimit, String currencyCode, String serviceCurrencyMatrixId,
			String isUpdateCustomerWithSpecialLimit);
	
	@Modifying
	@Query("update CustomerLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3 "
			+ " where serviceCurrencyMatrix.id = ?4 ")
	void updateAllLimits(BigDecimal maxAmountLimit, int maxOccurrenceLimit, String currencyCode, String serviceCurrencyMatrixId);
	
	@Modifying
	@Query("update CustomerLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?4 "
			+ "where limitPackage.code = ?3 "
			+ "and service.code =?5 "
			+ "and serviceCurrencyMatrix.id = ?6 ")
	void updateLimitsByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId);
	
	@Query("select distinct customer.id from CustomerLimitModel where limitPackage.code = ?1")
	List<String> findCustomersByLimitPackage(String limitPackageCode) throws Exception;
	
	@Modifying
	@Query("delete CustomerLimitModel where customer.id = ?1")
	void deleteByCustomerId(String customerId);
}
