package com.gpt.product.gpcash.limitpackage.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;

@Repository
public interface LimitPackageRepository extends JpaRepository<LimitPackageModel, String>, CashRepository<LimitPackageModel>{
	void deleteLimitPackageDetail(List<LimitPackageDetailModel> list) throws Exception;
	
	@Query("select service.code, service.name, serviceCurrencyMatrix.id, currencyMatrix.name, limitPackageDetail.currency.code, "
			 + "limitPackageDetail.maxOccurrenceLimit, limitPackageDetail.maxAmountLimit "
			 + "from LimitPackageDetailModel limitPackageDetail "
			 + "left join limitPackageDetail.limitPackage limitPackage "
			 + "left join limitPackageDetail.serviceCurrencyMatrix serviceCurrencyMatrix "
		     + "left join serviceCurrencyMatrix.service service "
		     + "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCurrencyMatrix.application.code = (?1) "
		     + "and service.serviceType.code = (?2) "
		     + "and limitPackage.code = (?3) "
			 + "order by service.idx ")
	Page<Object[]> findDetailByServiceTypeCodeAndLimitPackageCode(String applicationCode, String serviceTypeCode, String limitPackageCode, Pageable pageInfo) throws Exception;
	
	@Query("from LimitPackageDetailModel "
			+ "where service.code = ?1 "
			+ "and serviceCurrencyMatrix.id = ?2 "
			+ "and limitPackage.code = ?3")
	LimitPackageDetailModel findDetailByLimitPackageCode(String serviceCode, String serviceCurrencyMatrixId, String limitPackageCode) throws Exception;
	
	@Query("from LimitPackageModel h where h.code not in ("
			+ "select d.limitPackage.code from LimitPackageDetailModel d where "
			+ "d.serviceCurrencyMatrix.id = ?1 "
			+ ")")
	List<LimitPackageModel> findLimitPackageNotHaveServiceCurrency(String serviceCurrencyMatrixId) throws Exception;
	
	@Modifying
	@Query("update LimitPackageDetailModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3, updatedBy = ?4, updatedDate = ?5 "
			+ " where serviceCurrencyMatrix.id = ?6 ")
	void updateAllLimitsByServiceCurrencyMatrix(BigDecimal maxAmountLimit, int occurence, String currencyCode, String updatedBy, Date updatedDate, String serviceChargeId);
	
	@Modifying
	@Query("update LimitPackageDetailModel set maxAmountLimit = ?1, maxOccurrenceLimit = ?2, currency.code = ?3, updatedBy = ?4, updatedDate = ?5 "
			+ " where serviceCurrencyMatrix.id = ?6 and limitPackage.code = ?7")
	void updateLimitsByServiceCurrencyMatrix(BigDecimal maxAmountLimit, int occurence, String currencyCode, String updatedBy, Date updatedDate, 
			String serviceCurrencyMatrix, String limitPackageCode);
	
	@Query("from LimitPackageDetailModel where serviceCurrencyMatrix.id = ?1 and limitPackage.code = ?2 ")
	LimitPackageDetailModel findDetailByServiceCurrencyMatrixAndLimitPackage(String serviceChargeId, String limitPackage) throws Exception;
}
