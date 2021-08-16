package com.gpt.product.gpcash.corporate.token.tokentype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.token.tokentype.model.TokenTypeModel;

@Repository
public interface TokenTypeRepository extends JpaRepository<TokenTypeModel, String>, CashRepository<TokenTypeModel>{

}