package com.gpt.product.gpcash.cot.system.repository;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.cot.system.model.SystemCOTModel;

@Repository
public interface SystemCOTRepository extends JpaRepository<SystemCOTModel, String>, CashRepository<SystemCOTModel>{

	@Cacheable(cacheNames = "SystemCOT", key = "#p0 + #p1")
	SystemCOTModel findByCodeAndApplicationCode(String code, String applicationCode) throws Exception;

	@Override
	@CacheEvict(value = "SystemCOT", key = "#p0.code + #p0.application.code")
	<S extends SystemCOTModel> S save(S entity);
	
	@Query("from SystemCOTModel cot "
			+ "where application.code = ?1 "
			+ "order by endTime, name asc")
	List<SystemCOTModel> findByApplicationCodeSortByEndTime(String applicationCode) throws Exception;;
}
