package com.gpt.component.maintenance.errormapping.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface ErrorMappingRepository extends JpaRepository<ErrorMappingModel, String>, CashRepository<ErrorMappingModel> {

	@Query("from ErrorMappingModel where deleteFlag='N'")
	List<ErrorMappingModel> findAll();
	
	@Query("select code from ErrorMappingModel where deleteFlag='N'")
	List<String> findAllCodes();

	@Query("select code, name, errorFlag from ErrorMappingModel where deleteFlag='N' and code in ?1")
	List<Object[]> findAllEnMessages(Collection<String> codes);

	@Query("select code, nameId, errorFlag from ErrorMappingModel where deleteFlag='N' and code in ?1")
	List<Object[]> findAllIdMessages(Collection<String> codes);
	
}
