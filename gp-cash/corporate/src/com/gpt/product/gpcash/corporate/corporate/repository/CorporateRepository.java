package com.gpt.product.gpcash.corporate.corporate.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateContactModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;

@Repository
public interface CorporateRepository extends JpaRepository<CorporateModel, String>, CashRepository<CorporateModel>{

	@Query("from CorporateContactModel corpContact where corpContact.corporate.id = ?1 order by corpContact.idx")
	List<CorporateContactModel> findCorporateContact(String corporateId) throws Exception;
	
	@Modifying
	@Query("delete from CorporateContactModel corpContact where corpContact.corporate.id = ?1")
	void deleteByCorporateId(String corporateId);
	
	@Query("select corp.id from CorporateModel corp where corp.role.code = ?1 and deleteFlag = 'N' ")
	List<String> findCorporateByRoleCode(String roleCode) throws Exception;
	
	@Query("select corp.id from CorporateModel corp where corp.servicePackage.limitPackage.code = ?1 and deleteFlag = 'N' ")
	List<String> findCorporateByLimitPackageCode(String limitPackageCode) throws Exception;
	
	@Query("from CorporateModel corp where deleteFlag = 'N' ")
	List<CorporateModel> findCorporates() throws Exception;
	
	@Query("from CorporateModel corp where deleteFlag = 'N' and specialChargeFlag = 'N'")
	List<CorporateModel> findCorporatesWithoutSpecialCharge() throws Exception;
	
	@Query("from CorporateModel corp where deleteFlag = 'N' and specialLimitFlag = 'N'")
	List<CorporateModel> findCorporatesWithoutSpecialLimit() throws Exception;
	
	@Query("from CorporateModel corp where corp.createdDate between ?1 and ?2 order by corp.name")
	List<CorporateModel> findByDateBetween(Date startDate, Date endDate);

}
