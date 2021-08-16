package com.gpt.product.gpcash.retail.beneficiarylist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInternationalModel;

@Repository
public interface CustomerBeneficiaryListInternationalRepository extends JpaRepository<CustomerBeneficiaryListInternationalModel, String>, CashRepository<CustomerBeneficiaryListInternationalModel>{
	
	List<CustomerBeneficiaryListInternationalModel> findByBenAccountNoAndCustomerId(String accountNo, String customerId) throws Exception;
}