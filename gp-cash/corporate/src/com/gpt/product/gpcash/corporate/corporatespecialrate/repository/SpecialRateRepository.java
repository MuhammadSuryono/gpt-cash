package com.gpt.product.gpcash.corporate.corporatespecialrate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporatespecialrate.model.SpecialRateModel;

@Repository
public interface SpecialRateRepository extends JpaRepository<SpecialRateModel, String>, CashRepository<SpecialRateModel> {
	
	List<SpecialRateModel> findByDeleteFlag(String isDelete) throws Exception;

}
