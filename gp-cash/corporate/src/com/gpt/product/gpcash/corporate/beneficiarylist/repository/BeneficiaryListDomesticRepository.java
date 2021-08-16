package com.gpt.product.gpcash.corporate.beneficiarylist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;

@Repository
public interface BeneficiaryListDomesticRepository extends JpaRepository<BeneficiaryListDomesticModel, String>, CashRepository<BeneficiaryListDomesticModel>{
	
	List<BeneficiaryListDomesticModel> findByBenAccountNoAndCorporateId(String accountNo, String corporateId) throws Exception;
	
	List<BeneficiaryListDomesticModel> findByBenAccountNoAndCorporateIdAndIsBenOnline(String accountNo, String corporateId, String isOnline) throws Exception;
}