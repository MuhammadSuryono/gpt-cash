package com.gpt.product.gpcash.retail.logging.error.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.logging.error.model.CustomerErrorLogModel;

@Repository
public interface CustomerErrorLogRepository extends JpaRepository<CustomerErrorLogModel, String>, CashRepository<CustomerErrorLogModel>{

}
