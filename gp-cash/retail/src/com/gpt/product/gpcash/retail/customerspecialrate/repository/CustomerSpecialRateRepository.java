package com.gpt.product.gpcash.retail.customerspecialrate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.customerspecialrate.model.CustomerSpecialRateModel;

@Repository
public interface CustomerSpecialRateRepository extends JpaRepository<CustomerSpecialRateModel, String>, CashRepository<CustomerSpecialRateModel> {
	
	List<CustomerSpecialRateModel> findByDeleteFlag(String isDelete) throws Exception;

}
