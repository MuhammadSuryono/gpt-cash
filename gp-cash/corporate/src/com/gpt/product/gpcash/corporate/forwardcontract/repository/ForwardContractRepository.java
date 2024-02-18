package com.gpt.product.gpcash.corporate.forwardcontract.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.forwardcontract.model.ForwardContractModel;

@Repository
public interface ForwardContractRepository extends JpaRepository<ForwardContractModel, String>, CashRepository<ForwardContractModel> {
	
	List<ForwardContractModel> findByDeleteFlag(String isDelete) throws Exception;
	
	@Modifying
	@Query("update ForwardContractModel set trxAmountLimitUsage = trxAmountLimitUsage + ?1 where (trxAmountLimitUsage + ?1) <= trxAmountLimit "
			+ "and refNoContract =?2 ")
	int updateContractAmountLimit(BigDecimal transactionAmount, String id);

	ForwardContractModel findByRefNoContractAndRateTypeAndCorporate_IdAndStatusAndDeleteFlagAndExpiryDateGreaterThanEqual(String refNoContract, String rateType, String corpId, String status, String isDelete, Timestamp expiryDate);

}
