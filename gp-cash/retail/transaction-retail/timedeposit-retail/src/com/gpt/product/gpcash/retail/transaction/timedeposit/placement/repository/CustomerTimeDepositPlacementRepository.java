package com.gpt.product.gpcash.retail.transaction.timedeposit.placement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.timedeposit.placement.model.CustomerTimeDepositPlacementModel;

@Repository
public interface CustomerTimeDepositPlacementRepository extends JpaRepository<CustomerTimeDepositPlacementModel, String>, CashRepository<CustomerTimeDepositPlacementModel>{
	
	@Query("from CustomerTimeDepositPlacementModel custAccount "
			+ "where custAccount.customer.id = ?1 "
			+ "and custAccount.status = (?2)"
			+ "order by custAccount.placementDate")
	Page<CustomerTimeDepositPlacementModel> findByCustomerIdAndStatus(String customerId, String status, Pageable pageInfo) throws Exception;

}
