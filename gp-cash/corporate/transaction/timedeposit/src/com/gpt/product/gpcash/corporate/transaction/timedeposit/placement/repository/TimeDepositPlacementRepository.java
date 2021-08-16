package com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.model.TimeDepositPlacementModel;

@Repository
public interface TimeDepositPlacementRepository extends JpaRepository<TimeDepositPlacementModel, String>, CashRepository<TimeDepositPlacementModel>{

}
