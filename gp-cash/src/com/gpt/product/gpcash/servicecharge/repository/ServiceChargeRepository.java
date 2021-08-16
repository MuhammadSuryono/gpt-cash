package com.gpt.product.gpcash.servicecharge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;

@Repository
public interface ServiceChargeRepository extends JpaRepository<ServiceChargeModel, String>, CashRepository<ServiceChargeModel> {
	@Query("select service.code, service.name, serviceCharge.id, serviceCharge.name "
			 + "from ServiceChargeModel serviceCharge "
		     + "left join serviceCharge.service service "
		     + "where service.deleteFlag = 'N' "
		     + "and serviceCharge.application.code = (?1) "
		     + "and service.serviceType.code = (?2) "
			 + "order by service.idx, serviceCharge.idx ")
	Page<Object[]> findDetailByServiceTypeCode(String applicationCode, String serviceTypeCode, Pageable pageInfo) throws Exception;
}
