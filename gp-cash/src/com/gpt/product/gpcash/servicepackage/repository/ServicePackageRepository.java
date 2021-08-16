package com.gpt.product.gpcash.servicepackage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackageModel, String>, CashRepository<ServicePackageModel> {
	@Query("from ServicePackageModel service "
			+ "where service.menuPackage.code = ?1 "
			+ "and service.menuPackage.deleteFlag = 'N'")
	List<ServicePackageModel> findByMenuPackageCode(String menuPackageCode) throws Exception;
	
	@Query("from ServicePackageModel service "
			+ "where service.limitPackage.code = ?1 "
			+ "and service.limitPackage.deleteFlag = 'N'")
	List<ServicePackageModel> findByLimitPackageCode(String limitPackageCode) throws Exception;
	
	@Query("from ServicePackageModel service "
			+ "where service.chargePackage.code = ?1 "
			+ "and service.chargePackage.deleteFlag = 'N'")
	List<ServicePackageModel> findByChargePackageCode(String chargePackageCode) throws Exception;
}
