package com.gpt.product.gpcash.corporate.transaction.tax.kpp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.kpp.model.TaxKPPModel;

@Repository
public interface TaxKPPRepository extends JpaRepository<TaxKPPModel, String>, CashRepository<TaxKPPModel>{
	
}