package com.gpt.component.idm.passwordhistory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.idm.passwordhistory.model.IDMPasswordHistoryModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface IDMPasswordHistoryRepository extends JpaRepository<IDMPasswordHistoryModel, String>, CashRepository<IDMPasswordHistoryModel> {
	
	@Query("select h from IDMPasswordHistoryModel h where h.user.code = ?1 "
			+ "order by h.createdDate DESC")	
	List<IDMPasswordHistoryModel> findByUser(String userCode);
}

