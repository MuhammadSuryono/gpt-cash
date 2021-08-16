package com.gpt.component.idm.roletype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.roletype.model.IDMRoleTypeModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMRoleTypeRepository extends JpaRepository<IDMRoleTypeModel, String>, CashRepository<IDMRoleTypeModel> {

}
