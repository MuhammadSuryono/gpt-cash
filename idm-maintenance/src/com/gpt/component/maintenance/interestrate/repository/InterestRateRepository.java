package com.gpt.component.maintenance.interestrate.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.interestrate.model.InterestRateDetailModel;
import com.gpt.component.maintenance.interestrate.model.InterestRateModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRateModel, String>, CashRepository<InterestRateModel> {

	@Query("from InterestRateDetailModel intRt "
			+ "where intRt.interest.productCode = ?1 "
			+ "and intRt.balance = ?2 "
			+ "and intRt.period IS NULL "
			+ "and intRt.deleteFlag = 'N'")
	List<InterestRateDetailModel> findByProductCodeBalanceAndPeriodIsNull(String productCode, String balance) throws Exception;
	
	@Query("from InterestRateDetailModel intRt "
			+ "where intRt.interest.productCode = ?1 "
			+ "and intRt.balance = ?2 "
			+ "and intRt.period = ?3 "
			+ "and intRt.deleteFlag = 'N'")
	List<InterestRateDetailModel> findByProductCodeBalanceAndPeriod(String productCode, String balance, String period) throws Exception;
	
	List<InterestRateModel> findByProductCode(String productCode) throws Exception;
	
	@Query("from InterestRateDetailModel detail "
			+ "where detail.interest.id = ?1 ")
	Page<InterestRateDetailModel> findDetailByInterestId(String interestId, Pageable pageInfo) throws Exception;
	
	@Modifying
	@Query("delete from InterestRateDetailModel intDetail where intDetail.interest.id = ?1")
	void deleteByInterestRateId(String id);
	
	
	
}
