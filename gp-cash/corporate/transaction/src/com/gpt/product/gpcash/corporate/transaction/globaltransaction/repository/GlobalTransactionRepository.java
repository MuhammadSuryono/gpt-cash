package com.gpt.product.gpcash.corporate.transaction.globaltransaction.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.model.GlobalTransactionModel;

@Repository
public interface GlobalTransactionRepository extends JpaRepository<GlobalTransactionModel, String>, CashRepository<GlobalTransactionModel> {

	@Query("select distinct trx.corporateId from GlobalTransactionModel trx "
			+ "where trx.service.code = ?1 "
			+ "and trx.isError = 'N' "
			+ "and trx.createdDate between ?2 and ?3 ")
	List<String> findCorporatesByService(String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select SUM(totalChargeEquivalent) from GlobalTransactionModel trx "
			+ "where trx.corporateId = ?1 "
			+ "and trx.isError = 'N' "
			+ "and trx.service.code = ?2 "
			+ "and trx.createdDate between ?3 and ?4 ")
	BigDecimal sumTotalChargeByCorporateId(String corporateId, String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select COUNT(trx.id) from GlobalTransactionModel trx "
			+ "where trx.corporateId = ?1 "
			+ "and trx.isError = 'N' "
			+ "and trx.service.code = ?2 "
			+ "and trx.createdDate between ?3 and ?4 ")
	int countTotalChargeByCorporateId(String corporateId, String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select COUNT(trx.id) from GlobalTransactionModel trx "
			+ "where "
			+ "trx.isError = 'N' "
			+ "and trx.service.code = ?1 "
			+ "and trx.createdDate between ?2 and ?3 ")
	int countSuccessByServiceCode(String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select COUNT(trx.id) from GlobalTransactionModel trx "
			+ "where "
			+ "trx.isError = 'Y' "
			+ "and trx.service.code = ?1 "
			+ "and trx.createdDate between ?2 and ?3 ")
	int countFailedByServiceCode(String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select SUM(totalDebitedAmountEquivalent) from GlobalTransactionModel trx "
			+ "where "
			+ "trx.isError = 'N' "
			+ "and trx.service.code = ?1 "
			+ "and trx.createdDate between ?2 and ?3 ")
	BigDecimal sumSuccessByServiceCode(String serviceCode, Date startDate, Date endDate) throws Exception;
	
	@Query("select SUM(totalDebitedAmountEquivalent) from GlobalTransactionModel trx "
			+ "where "
			+ "trx.isError = 'Y' "
			+ "and trx.service.code = ?1 "
			+ "and trx.createdDate between ?2 and ?3 ")
	BigDecimal sumFailedByServiceCode(String serviceCode, Date startDate, Date endDate) throws Exception;
}
