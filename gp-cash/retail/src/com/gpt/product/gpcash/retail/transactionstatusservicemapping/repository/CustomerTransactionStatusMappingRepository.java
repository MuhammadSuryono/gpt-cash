package com.gpt.product.gpcash.retail.transactionstatusservicemapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transactionstatusservicemapping.model.CustomerTransactionStatusMappingModel;

@Repository
public interface CustomerTransactionStatusMappingRepository extends JpaRepository<CustomerTransactionStatusMappingModel, String>, CashRepository<CustomerTransactionStatusMappingModel>  {
	
}