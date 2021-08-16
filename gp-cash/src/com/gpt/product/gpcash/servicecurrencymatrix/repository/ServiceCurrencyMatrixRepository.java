package com.gpt.product.gpcash.servicecurrencymatrix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;

@Repository
public interface ServiceCurrencyMatrixRepository
		extends JpaRepository<ServiceCurrencyMatrixModel, String>, CashRepository<ServiceCurrencyMatrixModel> {
	@Query("select service.code, service.name, serviceCurrencyMatrix.id, currencyMatrix.name from ServiceCurrencyMatrixModel serviceCurrencyMatrix "
			+ "left join serviceCurrencyMatrix.service service "
			+ "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix " 
			+ "where service.deleteFlag = 'N' "
			+ "and serviceCurrencyMatrix.application.code = (?1) "
			+ "and service.serviceType.code = (?2) "
			+ "order by service.idx ")
	Page<Object[]> findDetailByServiceTypeCode(String applicationCode, String serviceTypeCode, Pageable pageInfo) throws Exception;

	@Query("select serviceCurrencyMatrix from ServiceCurrencyMatrixModel serviceCurrencyMatrix "
			+ "left join serviceCurrencyMatrix.service service "
			+ "left join serviceCurrencyMatrix.currencyMatrix currencyMatrix " 
			+ "where service.deleteFlag = 'N' "
			+ "and serviceCurrencyMatrix.application.code = (?1) "
			+ "and service.code = (?2) "
			+ "and currencyMatrix.code = ?3 " 
			+ "order by service.idx ")
	ServiceCurrencyMatrixModel findByServiceCode(String applicationCode, String serviceCode, String currencyMatrixCode) throws Exception;
}
