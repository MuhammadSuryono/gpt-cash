package com.gpt.product.gpcash.corporate.corporateuser.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;

@Repository
public interface CorporateUserRepository extends JpaRepository<CorporateUserModel, String>, CashRepository<CorporateUserModel> {
	@Query("select corp.id, corpUser.userId, usr.name, usr.stillLoginFlag, usr.code "
			 + "from CorporateUserModel corpUser "
		     + "left join corpUser.user usr "
		     + "left join corpUser.corporate corp "
		     + "where corp.id like (?1) "
		     + "and corpUser.userId like (?2) "
		     + "and usr.stillLoginFlag like (?3) "
		     + "and usr.deleteFlag = 'N' ")
	Page<Object[]> findStillLoginUsers(String corporateId, String userId, String stillLoginFlag, Pageable pageInfo) throws Exception;
	
	/**
	 * This method is actually the same as findOne, but it also directly/eagerly fecth IDMUserModel
	 * @param userCode
	 * @return
	 */
	@Query("from CorporateUserModel corpUser join fetch corpUser.user where corpUser.id = ?1")
	CorporateUserModel findOneFetchUser(String userCode);

	@Query("select corp.id, corpUser.userId, usr.name, usr.stillLoginFlag, usr.status, usr.code "
			 + "from CorporateUserModel corpUser "
		     + "left join corpUser.user usr "
		     + "left join corpUser.corporate corp "
		     + "where corp.id like (?1) "
		     + "and corpUser.userId like (?2) "
		     + "and usr.stillLoginFlag like (?3) "
		     + "and usr.status = (?4) "
		     + "and usr.deleteFlag = 'N' ")
	Page<Object[]> findUserByStatus(String corporateId, String userId, String stillLoginFlag, String status, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateUserModel corpUser "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corpUser.userId like (?2) "
		     + "and UPPER(corpUser.user.name) like UPPER(?3) "
		     + "and corpUser.corporateUserGroup.code like (?4) "
		     + "and corpUser.approvalMap.workflowRole.code not in ('CRP_ADM_MAKER', 'CRP_ADM_CHECKER') "
		     + "and corpUser.user.deleteFlag = 'N' ")
	Page<CorporateUserModel> findByUserIdNameAndGroupCode(String corporateId, String userId, String userName, String userGroupCode, Pageable pageInfo) throws Exception;
	
	@Query("from CorporateUserModel corpUsr "
			+ "where corpUsr.corporate.id = ?1 "
			+ "and corpUsr.user.deleteFlag = 'N' "
			+ "and corpUsr.approvalMap.workflowRole.code in ('CRP_ADM_MAKER', 'CRP_ADM_CHECKER')")
	List<CorporateUserModel> findAdminUser(String corporateId) throws Exception;
	
	@Query("from CorporateUserModel corpUsr "
			+ "where corpUsr.corporate.id = ?1 "
			+ "and corpUsr.user.deleteFlag = 'N' "
			+ "and corpUsr.approvalMap.workflowRole.code = 'CRP_ADM_CHECKER'")
	List<CorporateUserModel> findAdminUserChecker(String corporateId) throws Exception;
	
	@Query("from CorporateUserModel corpUsr "
			+ "where corpUsr.corporate.id = ?1 "
			+ "and corpUsr.user.deleteFlag = 'N' "
			+ "order by corpUsr.user.name")
	List<CorporateUserModel> findUserByCorporate(String corporateId) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
			 + "left join corporateUserGroup.corporateAccountGroup corporateAccountGroup "
			 + "left join corporateAccountGroup.corporateAccountGroupDetail corporateAccountGroupDetail "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corporateUserGroup.id = ?2 "
		     + "and corporateAccountGroupDetail.id like ?3 "
		     + "and corpUser.user.code in (?4) "
		     + "and corpUser.approvalMap.id in (?5) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserIncludeUserGroupIdForWorkflow(String corporateId, String userGroupId, String sourceAccountGroupDetailId, 
			List<String> authorizedUser, List<String> approvalMapList) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
			 + "left join corporateUserGroup.corporateAccountGroup corporateAccountGroup "
			 + "left join corporateAccountGroup.corporateAccountGroupDetail corporateAccountGroupDetail "
			 + "left join corporateAccountGroupDetail.corporateAccount corporateAccount "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corporateUserGroup.id = ?2 "
		     + "and corporateAccount.account.accountNo = ?3 "
		     + "and corpUser.user.code in (?4) "
		     + "and corpUser.approvalMap.id in (?5) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserForWorkflowSpesificGroup(String corporateId, String userGroupId, String corporateAccountId, 
			List<String> authorizedUser, List<String> approvalMapList) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corporateUserGroup.id = ?2 "
		     + "and corpUser.user.code in (?3) "
		     + "and corpUser.approvalMap.id in (?4) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserForWorkflowSpesificGroupNonTransaction(String corporateId, String userGroupId, 
			List<String> authorizedUser, List<String> approvalMapList) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
			 + "left join corporateUserGroup.corporateAccountGroup corporateAccountGroup "
			 + "left join corporateAccountGroup.corporateAccountGroupDetail corporateAccountGroupDetail "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corporateUserGroup.id != ?2 "
		     + "and corporateAccountGroupDetail.id like ?3 "
		     + "and corpUser.user.code in (?4) "
		     + "and corpUser.approvalMap.id in (?5) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserNotIncludeUserGroupIdForWorkflow(String corporateId, String userGroupId, String sourceAccountGroupDetailId, 
			List<String> authorizedUser, List<String> approvalMapList) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
			 + "left join corporateUserGroup.corporateAccountGroup corporateAccountGroup "
			 + "left join corporateAccountGroup.corporateAccountGroupDetail corporateAccountGroupDetail "
			 + "left join corporateAccountGroupDetail.corporateAccount corporateAccount "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corporateAccount.account.accountNo = ?2 "
		     + "and corpUser.user.code in (?3) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserForWorkflow(String corporateId, String corporateAccountId, List<String> authorizedUser) throws Exception;
	
	@Query("select corpUser from CorporateUserModel corpUser "
			 + "left join corpUser.corporateUserGroup corporateUserGroup "
		     + "where corpUser.corporate.id = ?1 "
		     + "and corpUser.user.code in (?2) "
		     + "and corpUser.user.deleteFlag = 'N' "
		     + "and corporateUserGroup.deleteFlag = 'N'")
	List<CorporateUserModel> findUserForWorkflowNonTransaction(String corporateId, List<String> authorizedUser) throws Exception;

	@Query("from CorporateUserModel corpUsr "
			+ "where corpUsr.corporate.id = ?1 and corpUsr.deleteFlag = ?2 "
			+ "and corpUsr.approvalMap.workflowRole.code not in ('CRP_ADM_MAKER', 'CRP_ADM_CHECKER')")
	List<CorporateUserModel> findByCorporateIdAndDeleteFlagExcludeAdmin(String corporateId, String deleteFlag) throws Exception;
	
	@Query("from CorporateUserModel corpUsr "
			+ "where corpUsr.corporate.id = ?1 and corpUsr.deleteFlag = ?2 "
			+ "and corpUsr.approvalMap.workflowRole.code in ('CRP_USR_MK', 'CRP_USR_MK_AP', 'CRP_USR_MK_AP_RL')")
	List<CorporateUserModel> findUserMakerByCorporateIdAndDeleteFlag(String corporateId, String deleteFlag) throws Exception;
	
	@Query("select corpUser.user.code from CorporateUserModel corpUser "
			+ "where corpUser.corporateUserGroup.corporate.id = ?1 "
			+ "and corpUser.approvalMap.id in (?2) "
			+ "and corpUser.user.deleteFlag = 'N' "
			+ "and corpUser.corporateUserGroup.deleteFlag = 'N'")
	List<String> findAuthorizedUserByApprovalMapForWorkflowANYUserGroup(String corporateId, List<String> approvalMapList) throws Exception;
	
	@Query("select corpUser.user.code from CorporateUserModel corpUser "
			+ "where corpUser.corporateUserGroup.corporate.id = ?1 "
			+ "and corpUser.approvalMap.id in (?2) "
			+ "and corpUser.corporateUserGroup.id = ?3 "
			+ "and corpUser.user.deleteFlag = 'N' "
			+ "and corpUser.corporateUserGroup.deleteFlag = 'N'")
	List<String> findAuthorizedUserByApprovalMapForWorkflowByUserGroup(String corporateId, List<String> approvalMapList,
			String makerUserGroupId) throws Exception;
	
	@Query("select corpUser.user.code from CorporateUserModel corpUser "
			+ "where corpUser.corporateUserGroup.corporate.id = ?1 "
			+ "and corpUser.approvalMap.id in (?2) "
			+ "and corpUser.corporateUserGroup.id <> ?3 "
			+ "and corpUser.user.deleteFlag = 'N' "
			+ "and corpUser.corporateUserGroup.deleteFlag = 'N'")
	List<String> findAuthorizedUserByApprovalMapForWorkflowCROSSUserGroup(String corporateId, List<String> approvalMapList,
			String makerUserGroupId) throws Exception;
	
	@Query("from CorporateUserModel corpUser "
			+ "where corpUser.corporateUserGroup.corporate.id = ?1 "
			+ "and corpUser.user.deleteFlag = 'N' "
			+ "and corpUser.corporateUserGroup.code = ?2")
	List<CorporateUserModel> findCorporateUserByUserGroupCode(String corporateId, String userGroupCode) throws Exception;
	
	@Query("from CorporateUserModel corpUser "
			+ "where corpUser.authorizedLimit.id = ?1 "
			+ "and corpUser.user.deleteFlag = 'N' ")
	List<CorporateUserModel> findCorporateUserByAuthorizedLimitId(String authorizedLimitId) throws Exception;
	
}
