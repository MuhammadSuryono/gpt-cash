package com.gpt.product.gpcash.chargepackage.repository;

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
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;

@Repository
public interface ChargePackageRepository extends JpaRepository<ChargePackageModel, String>, CashRepository<ChargePackageModel> {
	void deleteChargePackageDetail(List<ChargePackageDetailModel> list) throws Exception;
	
	@Query("select service.code, service.name, serviceCharge.id, serviceCharge.name,  "
			 + "chargePackageDetail.currency.code, chargePackageDetail.value, chargePackageDetail.valueType "
			 + "from ChargePackageDetailModel chargePackageDetail "
			 + "left join chargePackageDetail.chargePackage chargePackage "
			 + "left join chargePackageDetail.serviceCharge serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) "
		     + "and service.serviceType.code = (?2) "
		     + "and chargePackage.code = (?3)"
			 + "order by service.idx ")
	Page<Object[]> findDetailByServiceTypeCode(String applicationCode, String serviceTypeCode, String code, Pageable pageInfo) throws Exception;
	
	@Query("from ChargePackageModel h where h.code not in ("
			+ "select d.chargePackage.code from ChargePackageDetailModel d where "
			+ "d.serviceCharge.id = ?1 "
			+ ")")
	List<ChargePackageModel> findChargePackageNotHaveServiceCharge(String serviceChargeId) throws Exception;
	
	@Query("select h.code from ChargePackageModel h where h.code in ("
			+ "select d.chargePackage.code from ChargePackageDetailModel d where "
			+ "d.serviceCharge.id = ?1 "
			+ ")")
	List<String> findChargePackageHaveServiceCharge(String serviceChargeId) throws Exception;
	
	@Modifying
	@Query("update ChargePackageDetailModel set value = ?1, currency.code = ?2, updatedBy = ?3, updatedDate = ?4 "
			+ " where serviceCharge.id = ?5 ")
	void updateAllChargesByServiceCharge(BigDecimal chargeAmount, String currencyCode, String updatedBy, Date updatedDate, String serviceChargeId);
	
	@Modifying
	@Query("update ChargePackageDetailModel set value = ?1, currency.code = ?2, updatedBy = ?3, updatedDate = ?4 "
			+ " where serviceCharge.id = ?5 and chargePackage.code = ?6")
	void updateChargesByServiceCharge(BigDecimal chargeAmount, String currencyCode, String updatedBy, Date updatedDate, String serviceChargeId, String chargePackageCode);
	
	@Query("from ChargePackageDetailModel where serviceCharge.id = ?1 and chargePackage.code = ?2 ")
	ChargePackageDetailModel findDetailByServiceChargeAndChargePackage(String serviceChargeId, String chargePackage) throws Exception;
}
