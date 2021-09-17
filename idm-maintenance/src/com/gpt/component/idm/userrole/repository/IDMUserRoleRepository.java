package com.gpt.component.idm.userrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.userrole.model.IDMUserRoleModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMUserRoleRepository extends JpaRepository<IDMUserRoleModel, String>, CashRepository<IDMUserRoleModel> {
	
	void deleteByUserCode(String code);
	
	@Query("from IDMUserRoleModel userRole "
			+ "where userRole.user.code = ?1 "
			+ "and userRole.role.roleType.code = ?2 ")
	List<IDMUserRoleModel> findByUserCodeAndRoleTypeCode(String userCode, String roleType);
	
	@Query("select userRole.user.code from IDMUserRoleModel userRole "
			+ "where userRole.role.roleType.code = ?1 "
			+ "and userRole.role.code like ?2 "
			+ "and userRole.role.code like ?3")
	List<String> findByRoleTypeCodeAndWFRoleCode(String roleType, String wfRoleCode, String approvalLevel);
	

	@Query("select userRole.user.code from IDMUserRoleModel userRole "
			+ "where userRole.role.code = ?1 ")
	List<String>findByRoleCode(String roleCode);
}
