package com.gpt.product.gpcash.retail.customercharge.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;

@Repository
public interface CustomerChargeRepository extends JpaRepository<CustomerChargeModel, String>, CashRepository<CustomerChargeModel>{
	@Query("select customerCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "customerCharge.currency.code, customerCharge.currency.name, customerCharge.value, customerCharge.valueType from CustomerChargeModel customerCharge "
			 + "left join customerCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and service.serviceType.code = (?2) and customerCharge.customer.id = (?3)"
			 + "order by service.idx ")
	Page<Object[]> findCharges(String applicationCode, String serviceTypeCode, String customerId, Pageable pageInfo) throws Exception;
	
	@Query("select customerCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "customerCharge.currency.code, customerCharge.currency.name, customerCharge.value, customerCharge.valueType from CustomerChargeModel customerCharge "
			 + "left join customerCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and service.code = (?2) and customerCharge.customer.id = (?3)"
			 + "order by service.idx ")
	Page<Object[]> findChargesByCustomer(String applicationCode, String serviceCode, String customerId, Pageable pageInfo) throws Exception;
	
	@Query("select customerCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "customerCharge.currency.code, customerCharge.currency.name, customerCharge.value, customerCharge.valueType from CustomerChargeModel customerCharge "
			 + "left join customerCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and customerCharge.id in (?2)"
			 + "order by service.idx ")
	Page<Object[]> findChargesById(String applicationCode, List<String> customerChargeId, Pageable pageInfo) throws Exception;
	
	@Modifying
	@Query("update CustomerChargeModel set value = ?1, currency.code = ?3 "
			+ " where chargePackage.code = ?2 "
			+ "and serviceCharge.id = ?4 "
			+ "and customer.id in (select a.id from CustomerModel a where a.specialChargeFlag = ?5)")
	void updateChargesByChargePackageCode(BigDecimal chargeAmount,  
			String chargePackageCode, String currencyCode, String serviceChargeId,
			String isUpdateCustomerWithSpecialCharge);
	
	@Modifying
	@Query("update CustomerChargeModel set value = ?1, currency.code = ?3 "
			+ " where chargePackage.code = ?2 "
			+ "and serviceCharge.id = ?4 ")
	void updateChargesByChargePackageCode(BigDecimal chargeAmount,  
			String chargePackageCode, String currencyCode, String serviceChargeId);
	
	@Modifying
	@Query("update CustomerChargeModel set value = ?1, currency.code = ?2 "
			+ " where serviceCharge.id = ?3 "
			+ "and customer.id in (select a.id from CustomerModel a where a.specialChargeFlag = ?4)")
	void updateAllCharges(BigDecimal chargeAmount, String currencyCode, String serviceChargeId,
			String isUpdateCustomerWithSpecialCharge);
	
	@Modifying
	@Query("update CustomerChargeModel set value = ?1, currency.code = ?2 "
			+ " where serviceCharge.id = ?3 ")
	void updateAllCharges(BigDecimal chargeAmount, String currencyCode, String serviceChargeId);
	
	@Query("select distinct customer.id from CustomerChargeModel where chargePackage.code = ?1")
	List<String> findCustomersByChargePackage(String chargePackageCode) throws Exception;
	
	@Modifying
	@Query("delete CustomerChargeModel where customer.id = ?1")
	void deleteByCustomerId(String customerId);

}
