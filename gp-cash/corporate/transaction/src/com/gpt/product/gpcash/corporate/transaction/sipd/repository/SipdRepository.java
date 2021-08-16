package com.gpt.product.gpcash.corporate.transaction.sipd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.sipd.model.SipdModel;

@Repository
public interface SipdRepository extends JpaRepository<SipdModel, String>, CashRepository<SipdModel>{
	
	SipdModel findByBillIdAndCorporate_IdAndStatus(String txId, String corpId, String status);
}
