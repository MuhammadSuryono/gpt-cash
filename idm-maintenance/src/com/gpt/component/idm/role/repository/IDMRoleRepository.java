package com.gpt.component.idm.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMRoleRepository extends JpaRepository<IDMRoleModel, String>, CashRepository<IDMRoleModel> {
	
}
