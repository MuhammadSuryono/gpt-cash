package com.gpt.component.idm.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMApplicationRepository extends JpaRepository<IDMApplicationModel, String>, CashRepository<IDMApplicationModel> {

	@Query("select count(*) from IDMApplicationModel where code in (?1)")
	long countByCodes(List<String> codes);
	
}
