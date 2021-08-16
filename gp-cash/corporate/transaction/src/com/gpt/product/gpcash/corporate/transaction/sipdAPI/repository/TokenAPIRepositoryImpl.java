package com.gpt.product.gpcash.corporate.transaction.sipdAPI.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transaction.sipdAPI.model.TokenAPIModel;

public class TokenAPIRepositoryImpl extends CashRepositoryImpl<TokenAPIModel> {
	
	@PersistenceContext
	private EntityManager em;
	
}
