package com.gpt.product.gpcash.retail.beneficiarylist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;

@Repository
public interface CustomerBeneficiaryListDomesticRepository extends JpaRepository<CustomerBeneficiaryListDomesticModel, String>, CashRepository<CustomerBeneficiaryListDomesticModel>{
	
	List<CustomerBeneficiaryListDomesticModel> findByBenAccountNoAndCustomerId(String accountNo, String customerId) throws Exception;
	
	List<CustomerBeneficiaryListDomesticModel> findByBenAccountNoAndCustomerIdAndIsBenOnline(String accountNo, String corporateId, String isOnline) throws Exception;
}