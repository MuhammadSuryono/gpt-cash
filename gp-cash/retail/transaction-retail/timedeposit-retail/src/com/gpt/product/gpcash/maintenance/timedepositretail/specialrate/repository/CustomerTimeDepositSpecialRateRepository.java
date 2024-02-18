package com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.model.CustomerTimeDepositSpecialRateModel;

@Repository
public interface CustomerTimeDepositSpecialRateRepository extends JpaRepository<CustomerTimeDepositSpecialRateModel, String>, CashRepository<CustomerTimeDepositSpecialRateModel> {
	
	List<CustomerTimeDepositSpecialRateModel> findByDeleteFlag(String isDelete) throws Exception;

}
