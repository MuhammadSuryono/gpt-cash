package com.gpt.component.maintenance.promo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.promo.model.PromoModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface PromoRepository extends JpaRepository<PromoModel, String>, CashRepository<PromoModel> {
	
}
