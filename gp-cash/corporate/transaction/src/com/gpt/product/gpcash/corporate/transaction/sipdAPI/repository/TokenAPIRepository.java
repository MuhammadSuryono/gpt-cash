package com.gpt.product.gpcash.corporate.transaction.sipdAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.sipdAPI.model.TokenAPIModel;

@Repository
public interface TokenAPIRepository extends JpaRepository<TokenAPIModel, String>, CashRepository<TokenAPIModel> {
	

}
