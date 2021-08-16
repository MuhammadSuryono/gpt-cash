package com.gpt.product.gpcash.corporate.corporateusergroup.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;

@Repository
public interface CorporateUserGroupRepository extends JpaRepository<CorporateUserGroupModel, String>, CashRepository<CorporateUserGroupModel>{

	Page<CorporateUserGroupModel> searchUserGroupNotAdmin(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;
	
	CorporateUserGroupModel findByCorporateIdAndCode(String corporateId, String code) throws Exception;
	
	@Query("from CorporateUserGroupDetailModel detail "
			+ "where detail.corporateUserGroup.corporate.id = ?1 "
			+ "and detail.serviceCurrencyMatrix.id = ?2 "
			+ "and detail.corporateUserGroup.id = ?3")
	CorporateUserGroupDetailModel findDetailByServiceCurrencyMatrixIdAndUserGroupId(String corporateId, String serviceCurrencyMatrixId, String corporateUserGroupId) throws Exception;
	
	@Query("from CorporateUserGroupDetailModel detail "
			+ "where detail.corporateUserGroup.corporate.id = ?1 "
			+ "and detail.corporateUserGroup.id = ?2 order by service.name")
	List<CorporateUserGroupDetailModel> findDetailByUserGroupId(String corporateId, String corporateUserGroupId) throws Exception;

	@Modifying
	@Query("delete from CorporateUserGroupDetailModel detail where detail.corporateUserGroup.id= ?1")
	void deleteDetailByUserGroupId(String id);
	
	@Modifying
	@Query("delete from CorporateUserGroupDetailModel detail where detail.id = ?1")
	void deleteDetailById(String id);
	
	@Query("from CorporateUserGroupModel corpUserGroup "
			+ "where corpUserGroup.corporate.id = ?1 "
			+ "and corpUserGroup.role.roleType.code = ?2 "
			+ "and corpUserGroup.role.application.code = ?3 "
			+ "and corpUserGroup.deleteFlag = 'N' "
			+ "order by corpUserGroup.code")
	List<CorporateUserGroupModel> findByRoleTypeCodeAndApplicationCode(String corporateId, String roleTypeCode, String applicationCode) throws Exception;

	@Query("from CorporateUserGroupDetailModel detail "
			+ "where detail.corporateUserGroup.corporate.id = ?1 "
			+ "and detail.service.code =?2 "
			+ "and detail.serviceCurrencyMatrix.currencyMatrix.code = ?3 "
			+ "and detail.corporateUserGroup.id = ?4")
	CorporateUserGroupDetailModel findDetailByServiceCodeAndCurrencyMatrixCodeAndUserGroupId(String corporateId, String serviceCode, String currencyMatrixCode, String corporateUserGroupId) throws Exception;
	
	@Modifying
	@Query("update CorporateUserGroupDetailModel set maxAmountLimit = ?1 where service.code =?2 "
			+ "and serviceCurrencyMatrix.currencyMatrix.code = ?3 "
			+ "and currency.code = ?4 and corporateUserGroup.id in (?5) ")
	void updateAmountLimit(BigDecimal newMaxAmountLimit, String serviceTransactionCode, String serviceCurrencyMatrixId, 
			String currencyCode, List<String> corporateUserGroupIdList);
	
	@Modifying
	@Query("update CorporateUserGroupDetailModel set amountLimitUsage = 0")
	void resetLimit();
	
	@Modifying
	@Query("update CorporateUserGroupDetailModel detail "
			+ "set detail.amountLimitUsage = 0"
			+ "where "
			+ "detail.corporateUserGroup.id in (?1)")
	void resetLimitByUserGroupId(List<String> userGroupIdList);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("from CorporateUserGroupModel model "
			+ "where model.id = ?1 ")	
	CorporateUserGroupModel findByIdWithLocking(String id) throws Exception;	
	
	@Modifying
	@Query("update CorporateUserGroupDetailModel set amountLimitUsage = amountLimitUsage + ?1 where (amountLimitUsage + ?1) <= maxAmountLimit "
			+ "and id =?2 ")
	int updateUserGroupLimit(BigDecimal transactionAmount, String id);

	@Modifying
	@Query("update CorporateUserGroupDetailModel set amountLimitUsage = amountLimitUsage - ?1 where id =?2 ")
	void reverseUpdateUserGroupLimit(BigDecimal transactionAmount, String id);

	@Query("select corpUsrGroup.role.code from CorporateUserGroupModel corpUsrGroup "
			+ "where corpUsrGroup.corporate.id = ?1 and deleteFlag = 'N' ")
	List<String> findRoleCodeByCorporateId(String corporateId) throws Exception;
	
	@Query("from CorporateUserGroupModel corpUserGroup "
			+ "where corpUserGroup.corporate.id = ?1 "
			+ "and corpUserGroup.corporateAccountGroup.code = ?2 "
			+ "and corpUserGroup.deleteFlag ='N' ")
	List<CorporateUserGroupModel> findByCorporateIdAndAccountGroupCode(String corporateId, String accountGroupCode) throws Exception;
	
	@Query("select detail.id from CorporateUserGroupDetailModel detail "
			+ "where detail.corporateUserGroup.corporate.servicePackage.limitPackage.code = ?1 ")
	List<String> findUserGroupIdByLimitPackageCode(String limitPackageCode) throws Exception;
	
	@Modifying
	@Query("delete from CorporateUserGroupDetailModel detail "
			+ "where "
			+ "detail.corporateUserGroup.id in (?1) and "
			+ "detail.service.code not in (?2)")
	void deleteDetailByCorporateIdAndServiceCode(List<String> corporateIdList, List<String> serviceCodeList);
}
