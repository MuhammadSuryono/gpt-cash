package com.gpt.product.gpcash.corporate.transaction.tax.mpn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.mpn.model.MPNModel;

@Repository
public interface MPNRepository extends JpaRepository<MPNModel, String>, CashRepository<MPNModel> {
	@Query("select p from MPNModel p where p.ntpn is null order by p.corporate.id, p.paymentDate, p.referenceNo")	
	Page<MPNModel> findPendingNTP(Pageable pageInfo);
	
	@Query("select p.ntpn from MPNModel p where p.referenceNo = ?1")	
	String findNTPByRefNo(String refNo);
}
