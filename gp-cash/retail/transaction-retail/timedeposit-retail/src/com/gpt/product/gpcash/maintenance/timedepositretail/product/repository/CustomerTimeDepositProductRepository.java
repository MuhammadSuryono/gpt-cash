package com.gpt.product.gpcash.maintenance.timedepositretail.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositProductModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositTermModel;

@Repository
public interface CustomerTimeDepositProductRepository extends JpaRepository<CustomerTimeDepositProductModel, String>, CashRepository<CustomerTimeDepositProductModel> {
	
}
