package com.gpt.product.gpcash.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceModel, String>, CashRepository<ServiceModel>{
	@Query("select distinct srvc.menu.code, srvc.menu.name from ServiceModel srvc "
			+ "where srvc.serviceType.code = ?1 "
			+ "and srvc.inactiveFlag = 'N' "
			+ "and srvc.deleteFlag = 'N' "
			+ "order by srvc.menu.name ")
	List<Object[]> findMenuByServiceType(String serviceTypeCode) throws Exception;	
	
	@Query("select srvc.code, srvc.name from ServiceModel srvc "
			+ "where srvc.serviceType.code = ?1 "
			+ "and srvc.inactiveFlag = 'N' "
			+ "and srvc.deleteFlag = 'N' "
			+ "order by srvc.name ")
	List<Object[]> findServiceByServiceType(String serviceTypeCode) throws Exception;
	
	@Query("select srvc.code, srvc.name from ServiceModel srvc "
			+ "where srvc.menu.code = ?1 "
			+ "and srvc.inactiveFlag = 'N' "
			+ "and srvc.deleteFlag = 'N' "
			+ "order by srvc.name ")
	List<Object[]> findServiceByMenuCode(String menuCode) throws Exception;
	
	@Query("select srvc.code, srvc.name from ServiceModel srvc "
			+ "where srvc.code like '%_DOM_%'"
			+ "and srvc.inactiveFlag = 'N' "
			+ "and srvc.deleteFlag = 'N' "
			+ "order by srvc.name ")
	List<Object[]> findDomesticService() throws Exception;
}
