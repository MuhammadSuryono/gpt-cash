package com.gpt.component.idm.user.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMUserRepository extends JpaRepository<IDMUserModel, String>, CashRepository<IDMUserModel> {

	@Modifying
	@Query("update IDMUserModel set stillLoginFlag = 'N' where code = ?1 and lastLoginDate = ?2")
	void releaseLoginSession(String userId, Timestamp loginDate);
	
	@Query("select b.code, b.name, a.application.code, b.stillLoginFlag "
			+ "from IDMUserAppModel a "
			+ "left join  a.user b where b.stillLoginFlag like (?1) "
			+ "and a.application.code is not null "
			+ "and a.application.code like (?2) "
			+ "and b.code like (?3) "
			+ "and b.deleteFlag = 'N' "
			+ "order by a.application.code")
	Page<Object[]> findStillLoginUser(String stillLoginFlag, String applicationCode, String userId, Pageable pageInfo)
			throws Exception;

	@Modifying
	@Query("update IDMUserModel u set u.stillLoginFlag = 'N' where u.code in ?1")
	void updateStillLoginFlagByCodes(List<String> userList);

	@Modifying
	@Query("update IDMUserModel a set stillLoginFlag = 'N' "
			+ "where a.code in "
			+ "(select b.code "
				+ "from IDMUserAppModel a "
				+ "left join a.user b "
				+ "where b.deleteFlag = 'N' "
				+ "and a.application.code in (?1))")
	void updateStillLoginFlagByApplicationCodes(List<String> applicationCode);

	@Modifying
	@Query("update IDMUserModel u set u.status = 'ACTIVE' where u.code in ?1")
	void unlockUsers(List<String> userList);
	
	@Modifying
	@Query("update IDMUserModel u set u.status = ?1 where u.code in ?2")
	int lockUnlockUser(String lockUnlockStatus, String userCode);

	@Query("select user from IDMUserAppModel userApp " 
	        + "left join userApp.user user "
			+ "where user.code = ?1 "
			+ "and userApp.application.code in (?2) "
			+ "and user.deleteFlag = 'N' ")
	Page<IDMUserModel> findUserApplication(String code, List<String> applicationCodeList, Pageable pageInfo);

	@Query("select user from IDMUserAppModel userApp " 
			+ "left join userApp.user user "
			+ "where user.code like ?1 "
			+ "and UPPER(user.name) like ?2 "
			+ "and userApp.application.code in (?3) "
			+ "and user.branch.code like ?4 "
			+ "and user.deleteFlag = 'N' ")
	Page<IDMUserModel> findIDMUser(String code, String name, List<String> applicationCodeList, String branchCode, Pageable pageInfo);

	@Query("select approvalMap.id from ApprovalMapModel approvalMap "
			+ "where approvalMap.workflowRole.wfCat = (?1) "
			+ "and approvalMap.workflowRole.code like (?2) "
			+ "and approvalMap.approvalLevel.code like (?3) ")
	List<String> findApprovalMapForWorkflow(String workflowCategory, String workflowRoleCode,
			String approvalLevelCode) throws Exception;
	
	@Query("select usr from IDMUserRoleModel userRole " 
			+ "left join userRole.user usr "
			+ "where userRole.role.code in ("
			+ "select roleMenu.role.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.menu.code = (?1) and roleMenu.role.roleType.code = 'AP') "
			+ "and usr.deleteFlag = 'N' ")
	List<IDMUserModel> findAuthorizedUserBankForWorkflow(String menuCode) throws Exception;
	
	@Query("select usr.code from IDMUserRoleModel userRole " 
			+ "left join userRole.user usr "
			+ "where userRole.role.code in ("
			+ "select roleMenu.role.code from IDMRoleMenuModel roleMenu "
			+ "where roleMenu.menu.code = (?1) and roleMenu.role.roleType.code = 'AP') "
			+ "and usr.code in (?2) "
			+ "and usr.deleteFlag = 'N' ")
	List<String> findAuthorizedUserForWorkflow(String menuCode, List<String> userList) throws Exception;
	
	@Query("select usr.code from IDMUserModel usr "
			+ "left join usr.branch br "
			+ "where br.code = ?1 " 
			+ "and usr.code in (?2) "
			+ "and usr.deleteFlag = 'N' ")
	List<String> findUserByBranchForWorkflow(String branchCode, List<String> userList) throws Exception;
	
	@Modifying
	@Query("update IDMUserModel set status = ?1 , lastLockedFromIdleDate = ?3 where code = ?2")
	void updateUserToInactive(String expiredStatus, String userCode, Timestamp lastLockedFromIdleDate);
	
	@Query("select user from IDMUserAppModel userApp "
			+ "left join userApp.user user "
			+ "where user.deleteFlag = 'N' "
			+ "and userApp.application.code = ?1 "
			+ "order by user.name")
	List<IDMUserModel> findBankUsers(String bankApplicationCode);
}
