package com.gpt.product.gpcash.corporate.transaction.sipd.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transaction.sipd.model.SipdModel;

public class SipdRepositoryImpl extends CashRepositoryImpl<SipdModel> {
	
	@PersistenceContext
	private EntityManager em;
	
}
