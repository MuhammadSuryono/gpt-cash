package com.gpt.component.idm.userapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.userapp.model.IDMUserAppModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMUserAppRepository extends JpaRepository<IDMUserAppModel, String>, CashRepository<IDMUserAppModel> {
	
	void deleteByUserCode(String code);
	
	@Query("select app from IDMUserAppModel userApp "
			+ "join userApp.application app "
			+ "where userApp.user.code = ?1")
	List<IDMApplicationModel> findApplicationsByUserCode(String userCode);
	
	@Query("select app.code from IDMUserAppModel userApp "
			+ "join userApp.application app "
			+ "where userApp.user.code = ?1")
	List<String> findApplicationsCodeByUserCode(String userCode);
	
}
