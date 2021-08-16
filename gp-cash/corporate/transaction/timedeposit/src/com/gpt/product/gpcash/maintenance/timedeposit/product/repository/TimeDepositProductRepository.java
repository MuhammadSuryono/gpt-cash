package com.gpt.product.gpcash.maintenance.timedeposit.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.maintenance.timedeposit.product.model.TimeDepositProductModel;
import com.gpt.product.gpcash.maintenance.timedeposit.product.model.TimeDepositTermModel;

@Repository
public interface TimeDepositProductRepository extends JpaRepository<TimeDepositProductModel, String>, CashRepository<TimeDepositProductModel> {
	
}
