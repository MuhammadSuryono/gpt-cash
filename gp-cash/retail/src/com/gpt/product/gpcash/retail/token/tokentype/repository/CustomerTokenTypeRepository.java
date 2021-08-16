package com.gpt.product.gpcash.retail.token.tokentype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.token.tokentype.model.CustomerTokenTypeModel;

@Repository
public interface CustomerTokenTypeRepository extends JpaRepository<CustomerTokenTypeModel, String>, CashRepository<CustomerTokenTypeModel>{

}