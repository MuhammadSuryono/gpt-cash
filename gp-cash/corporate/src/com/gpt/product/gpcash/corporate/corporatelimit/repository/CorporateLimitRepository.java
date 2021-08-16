package com.gpt.product.gpcash.corporate.corporatelimit.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;

@Repository
public interface CorporateLimitRepository extends JpaRepository<CorporateLimitModel, String>, CashRepository<CorporateLimitModel> {
	@Query("select corporateLimit.id, corporateLimit.service.code, service.name, corporateLimit.maxAmountLimit, corporateLimit.maxOccurrenceLimit, "
			 + "corporateLimit.amountLimitUsage, corporateLimit.occurrenceLimitUsage, corporateLimit.currency.code, "
			 + "currency.name, currencyMatrix.code, currencyMatrix.name from CorporateLimitModel corporateLimit "
		     + "left join corporateLimit.service service "
		     + "left join corporateLimit.currency currency "
		     + "left join corporateLimit.serviceCurrencyMatrix serviceCurrencyMatrix "
			 + "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix "
			 + "where corporateLimit.application.code = (?1) and service.serviceType.code = (?2) and corporateLimit.corporate.id = (?3) and service.deleteFlag = 'N' order by service.idx")
	Page<Object[]> findLimit(String applicationCode, String serviceTypeCode, String corporateId, Pageable pageInfo) throws Exception;
		
	@Query("select corporateLimit.id, corporateLimit.service.code, service.name, corporateLimit.maxAmountLimit, corporateLimit.maxOccurrenceLimit, "
			+ "corporateLimit.amountLimitUsage, corporateLimit.occurrenceLimitUsage, corporateLimit.currency.code, "
			+ "currency.name, currencyMatrix.code, currencyMatrix.name from CorporateLimitModel corporateLimit "
			+ "left join corporateLimit.service service "
			+ "left join corporateLimit.currency currency "
			+ "left join corporateLimit.serviceCurrencyMatrix serviceCurrencyMatrix "
			+ "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix "
			+ "where corporateLimit.application.code = (?1) and corporateLimit.id in (?2) and service.deleteFlag = 'N' order by service.idx")
	Page<Object[]> findLimit(String applicationCode, List<String> corporateLimitId, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateLimitModel a "
			 + "where a.application.code = ?1 "
			 + "and a.service.code = ?2 "
			 + "and a.serviceCurrencyMatrix.currencyMatrix.code = ?3 "
			 + "and a.currency.code = ?4 "
			 + "and a.corporate.id = ?5 "
			 + "and a.service.deleteFlag = 'N'")
	CorporateLimitModel findByServiceAndCurrencyMatrix(String applicationCode, String serviceCode, String currencyMatrixCode, 
			String currencyCode, String corporateId) throws Exception;
	
	@Modifying
	@Query("update CorporateLimitModel set amountLimitUsage = 0, occurrenceLimitUsage = 0")
	void resetLimit();
	
	@Modifying
	@Query("update CorporateLimitModel set amountLimitUsage = amountLimitUsage + ?1, occurrenceLimitUsage = occurrenceLimitUsage + 1 "
			+ "where (amountLimitUsage + ?1) <= maxAmountLimit and occurrenceLimitUsage <= maxOccurrenceLimit "
			+ "and id =?2 ")
	void updateCorporateLimit(BigDecimal transactionAmount, String id);
	
	@Modifying
	@Query("update CorporateLimitModel set amountLimitUsage = amountLimitUsage - ?1, occurrenceLimitUsage = occurrenceLimitUsage - 1 " 
			+ "where id =?2 ")
	void reverseUpdateCorporateLimit(BigDecimal transactionAmount, String id);
	
	@Modifying
	@Query("update CorporateLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?4 "
			+ "where limitPackage.code = ?3 "
			+ "and service.code =?5 "
			+ "and serviceCurrencyMatrix.id = ?6 "
			+ "and corporate.id in "
				+ "(select a.id from CorporateModel a "
				+ "where a.specialLimitFlag = ?7)")
	void updateLimitsByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId,
			String isUpdateCorporateWithSpecialLimit);
	
	@Modifying
	@Query("update CorporateLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3 "
			+ " where serviceCurrencyMatrix.id = ?4 "
			+ "and corporate.id in (select a.id from CorporateModel a where a.specialChargeFlag = ?5)")
	void updateAllLimits(BigDecimal maxAmountLimit, int maxOccurrenceLimit, String currencyCode, String serviceCurrencyMatrixId,
			String isUpdateCorporateWithSpecialLimit);
	
	@Modifying
	@Query("update CorporateLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3 "
			+ " where serviceCurrencyMatrix.id = ?4 ")
	void updateAllLimits(BigDecimal maxAmountLimit, int maxOccurrenceLimit, String currencyCode, String serviceCurrencyMatrixId);
	
	@Modifying
	@Query("update CorporateLimitModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?4 "
			+ "where limitPackage.code = ?3 "
			+ "and service.code =?5 "
			+ "and serviceCurrencyMatrix.id = ?6 ")
	void updateLimitsByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId);
	
	@Query("select distinct corporate.id from CorporateLimitModel where limitPackage.code = ?1")
	List<String> findCorporatesByLimitPackage(String limitPackageCode) throws Exception;
	
	@Modifying
	@Query("delete CorporateLimitModel where corporate.id = ?1")
	void deleteByCorporateId(String corporateId);
}
