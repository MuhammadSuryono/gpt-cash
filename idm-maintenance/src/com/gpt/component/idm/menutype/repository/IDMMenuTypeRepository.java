package com.gpt.component.idm.menutype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.menutype.model.IDMMenuTypeModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMMenuTypeRepository extends JpaRepository<IDMMenuTypeModel, String>, CashRepository<IDMMenuTypeModel> {

}
