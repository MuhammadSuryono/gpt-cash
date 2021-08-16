package com.gpt.product.gpcash.corporate.transaction.va.registration.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationDetailModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationModel;

@Repository
public interface VARegistrationRepository extends JpaRepository<VARegistrationModel, String>, CashRepository<VARegistrationModel>{
	@Query("from VARegistrationModel vaRegistration "
			+ "where vaRegistration.corporate.id = ?1 "
			+ "and vaRegistration.mainAccountNo.accountNo = ?2 "
			+ "and vaRegistration.productCode = ?3 ")
	VARegistrationModel findByCorporateIdAndAccountNo(String corporateId, String accountNo, String productCode) throws Exception;
	
	@Query("select distinct vaRegistration.mainAccountNo.accountNo from VARegistrationModel vaRegistration "
			+ "where vaRegistration.corporate.id = ?1 "
			+ "and vaRegistration.mainAccountNo.accountNo in (?2) order by vaRegistration.mainAccountNo.accountNo")
	List<String> findByRegisteredAccountNo(String corporateId, List<String> accountList) throws Exception;
	
	@Modifying
	@Query("delete from VARegistrationModel vaRegistration where vaRegistration.corporate.id = ?1")
	void deleteByCorporateId(String corporateId);
	
	@Modifying
	@Query("delete from VARegistrationDetailModel detail where detail.vaNo = ?1")
	void deleteDetailByVANo(String vaNo);
	
	@Modifying
	@Query("delete from VARegistrationDetailModel detail where detail.vaRegistration.id= ?1")
	void deleteDetailByVAId(String id);
	
	@Query("from VARegistrationModel vaRegistration "
			+ "where vaRegistration.corporate.id = ?1 "
			+ "and vaRegistration.mainAccountNo.accountNo in (?2) order by vaRegistration.mainAccountNo.accountNo")
	Page<VARegistrationModel> findByCorporateIdAndAccountNo(String corporateId, List<String> accountList,
			Pageable pageInfo) throws Exception;
	
	@Query("from VARegistrationDetailModel detail "
			+ "where detail.vaRegistration.id = ?1  order by detail.idx")
	Page<VARegistrationDetailModel> findVADetailListByVARegisId(String vaRegistrationId,
			Pageable pageInfo) throws Exception;
	
	@Query("from VARegistrationDetailModel detail "
			+ "where detail.vaNo = ?1 ")
	VARegistrationDetailModel findVADetailListByVANo(String vaNo) throws Exception;
	
	@Query("select distinct vaRegistration.productCode from VARegistrationModel vaRegistration "
			+ "where vaRegistration.corporate.id = ?1 "
			+ "and vaRegistration.mainAccountNo.accountNo in (?2) order by vaRegistration.productCode")
	List<String> findProductCodeByRegisteredAccountNo(String corporateId, List<String> accountList) throws Exception;
	
	@Query("select distinct vaRegistration.productCode from VARegistrationModel vaRegistration "
			+ "where vaRegistration.corporate.id = ?1 "
			+ "and vaRegistration.mainAccountNo.accountNo in (?2) order by vaRegistration.productCode")
	List<String> findProductByCorporateIdAndRegisteredAccountNo(String corporateId, List<String> accountList) throws Exception;
	
	@Query("select distinct va.productCode, va.productName from VARegistrationModel va "
			+ "where va.corporate.id = ?1 "
			+ "and va.mainAccountNo.accountNo = ?2 order by va.productCode")
	List<Object[]> findProductByCorporateIdAndAccountNo(String corporateId, String accountNo) throws Exception;
	
	@Query("select va.productName from VARegistrationModel va "
			+ "where va.corporate.id = ?1 "
			+ "and va.mainAccountNo.accountNo = ?2 "
			+ "and va.productCode = ?3 ")
	Object findProductName(String corporateId, String accountNo, String productCode) throws Exception;
}