package com.gpt.component.maintenance.exchangerate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateModel, String>, CashRepository<ExchangeRateModel> {
	
	List<ExchangeRateModel> findByDeleteFlag(String isDelete) throws Exception;

}
