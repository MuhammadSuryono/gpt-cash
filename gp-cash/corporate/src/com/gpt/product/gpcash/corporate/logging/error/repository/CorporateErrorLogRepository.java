package com.gpt.product.gpcash.corporate.logging.error.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.logging.error.model.CorporateErrorLogModel;

@Repository
public interface CorporateErrorLogRepository extends JpaRepository<CorporateErrorLogModel, String>, CashRepository<CorporateErrorLogModel>{

}
