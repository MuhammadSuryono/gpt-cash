package com.gpt.product.gpcash.corporate.transaction.tax.taxtype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.model.TaxTypeModel;

@Repository
public interface TaxTypeRepository extends JpaRepository<TaxTypeModel, String>, CashRepository<TaxTypeModel>{

}