package com.gpt.product.gpcash.retail.beneficiarylist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInHouseModel;

@Repository
public interface CustomerBeneficiaryListInHouseRepository extends JpaRepository<CustomerBeneficiaryListInHouseModel, String>, CashRepository<CustomerBeneficiaryListInHouseModel>{

	List<CustomerBeneficiaryListInHouseModel> findByBenAccountNoAndCustomerId(String accountNo, String customerId) throws Exception;
	
}