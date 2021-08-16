package com.gpt.product.gpcash.corporate.corporatetdspecialrate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporatetdspecialrate.model.TDSpecialRateModel;

@Repository
public interface TDSpecialRateRepository extends JpaRepository<TDSpecialRateModel, String>, CashRepository<TDSpecialRateModel> {
	
	List<TDSpecialRateModel> findByDeleteFlag(String isDelete) throws Exception;

}
