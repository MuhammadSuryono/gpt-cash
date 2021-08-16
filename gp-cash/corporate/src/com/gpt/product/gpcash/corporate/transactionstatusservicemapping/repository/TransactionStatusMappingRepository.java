package com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;

@Repository
public interface TransactionStatusMappingRepository extends JpaRepository<TransactionStatusMappingModel, String>, CashRepository<TransactionStatusMappingModel>  {
	
}