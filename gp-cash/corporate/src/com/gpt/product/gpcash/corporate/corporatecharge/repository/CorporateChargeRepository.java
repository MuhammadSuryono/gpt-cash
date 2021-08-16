package com.gpt.product.gpcash.corporate.corporatecharge.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;

@Repository
public interface CorporateChargeRepository extends JpaRepository<CorporateChargeModel, String>, CashRepository<CorporateChargeModel>{
	@Query("select corporateCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "corporateCharge.currency.code, corporateCharge.currency.name, corporateCharge.value, corporateCharge.valueType from CorporateChargeModel corporateCharge "
			 + "left join corporateCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and service.serviceType.code = (?2) and corporateCharge.corporate.id = (?3)"
			 + "order by service.idx ")
	Page<Object[]> findCharges(String applicationCode, String serviceTypeCode, String corporateId, Pageable pageInfo) throws Exception;
	
	@Query("select corporateCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "corporateCharge.currency.code, corporateCharge.currency.name, corporateCharge.value, corporateCharge.valueType from CorporateChargeModel corporateCharge "
			 + "left join corporateCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and service.code = (?2) and corporateCharge.corporate.id = (?3)"
			 + "order by service.idx ")
	Page<Object[]> findChargesByCorporate(String applicationCode, String serviceCode, String corporateId, Pageable pageInfo) throws Exception;
	
	@Query("select corporateCharge.id, service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "corporateCharge.currency.code, corporateCharge.currency.name, corporateCharge.value, corporateCharge.valueType from CorporateChargeModel corporateCharge "
			 + "left join corporateCharge.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) and corporateCharge.id in (?2)"
			 + "order by service.idx ")
	Page<Object[]> findChargesById(String applicationCode, List<String> corporateChargeId, Pageable pageInfo) throws Exception;
	
	@Modifying
	@Query("update CorporateChargeModel set value = ?1, currency.code = ?3 "
			+ " where chargePackage.code = ?2 "
			+ "and serviceCharge.id = ?4 "
			+ "and corporate.id in (select a.id from CorporateModel a where a.specialChargeFlag = ?5)")
	void updateChargesByChargePackageCode(BigDecimal chargeAmount,  
			String chargePackageCode, String currencyCode, String serviceChargeId,
			String isUpdateCorporateWithSpecialCharge);
	
	@Modifying
	@Query("update CorporateChargeModel set value = ?1, currency.code = ?3 "
			+ " where chargePackage.code = ?2 "
			+ "and serviceCharge.id = ?4 ")
	void updateChargesByChargePackageCode(BigDecimal chargeAmount,  
			String chargePackageCode, String currencyCode, String serviceChargeId);
	
	@Modifying
	@Query("update CorporateChargeModel set value = ?1, currency.code = ?2 "
			+ " where serviceCharge.id = ?3 "
			+ "and corporate.id in (select a.id from CorporateModel a where a.specialChargeFlag = ?4)")
	void updateAllCharges(BigDecimal chargeAmount, String currencyCode, String serviceChargeId,
			String isUpdateCorporateWithSpecialCharge);
	
	@Modifying
	@Query("update CorporateChargeModel set value = ?1, currency.code = ?2 "
			+ " where serviceCharge.id = ?3 ")
	void updateAllCharges(BigDecimal chargeAmount, String currencyCode, String serviceChargeId);
	
	@Query("select distinct corporate.id from CorporateChargeModel where chargePackage.code = ?1")
	List<String> findCorporatesByChargePackage(String chargePackageCode) throws Exception;
	
	@Modifying
	@Query("delete CorporateChargeModel where corporate.id = ?1")
	void deleteByCorporateId(String corporateId);
}
