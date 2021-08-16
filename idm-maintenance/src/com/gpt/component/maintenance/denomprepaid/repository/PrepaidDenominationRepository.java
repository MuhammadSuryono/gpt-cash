package com.gpt.component.maintenance.denomprepaid.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.denomprepaid.model.PrepaidDenominationModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface PrepaidDenominationRepository extends JpaRepository<PrepaidDenominationModel, String>, CashRepository<PrepaidDenominationModel>{
	
	List<PrepaidDenominationModel> findByDenomination(String depositCode) throws Exception;
	
	Page<PrepaidDenominationModel> findByPrepaidType_CodeAndDeleteFlagOrderByDenominationAsc(String denomTypeCode, String isDelete, Pageable pageInfo) throws Exception;

}