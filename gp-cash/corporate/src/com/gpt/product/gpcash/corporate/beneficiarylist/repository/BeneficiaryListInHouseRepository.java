package com.gpt.product.gpcash.corporate.beneficiarylist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;

@Repository
public interface BeneficiaryListInHouseRepository extends JpaRepository<BeneficiaryListInHouseModel, String>, CashRepository<BeneficiaryListInHouseModel>{

	List<BeneficiaryListInHouseModel> findByBenAccountNoAndCorporateId(String accountNo, String corporateId) throws Exception;
	
}