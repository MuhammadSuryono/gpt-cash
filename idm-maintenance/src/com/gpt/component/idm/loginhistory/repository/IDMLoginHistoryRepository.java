package com.gpt.component.idm.loginhistory.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.loginhistory.model.IDMLoginHistoryModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMLoginHistoryRepository extends JpaRepository<IDMLoginHistoryModel, String>, CashRepository<IDMLoginHistoryModel> {

	List<IDMLoginHistoryModel> findByUserCodeIn(List<String> userCodeList);

	@Modifying
	@Query("update IDMLoginHistoryModel set logoutDate = ?2 where id = ?1")
	void updateLogoutDate(String id, Timestamp date);
	
	@Modifying
	@Query("update IDMLoginHistoryModel set logoutDate = ?2 where id in (?1)")
	void updateLogoutDate(List<String> ids, Timestamp date);

}
