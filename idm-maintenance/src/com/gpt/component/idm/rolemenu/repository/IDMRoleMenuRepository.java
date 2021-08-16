package com.gpt.component.idm.rolemenu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMRoleMenuRepository extends JpaRepository<IDMRoleMenuModel, String>, CashRepository<IDMRoleMenuModel> {

	void deleteByRoleCode(String code);
	
	List<IDMRoleMenuModel> searchRoleMenu(String roleCode) throws Exception;
}
