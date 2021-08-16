package com.gpt.product.gpcash.approvallevel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;

@Repository
public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevelModel, String>, CashRepository<ApprovalLevelModel>{

	@Query("from ApprovalLevelModel order by idx, code")
	List<ApprovalLevelModel> findApprovalLevelAndSort() throws Exception;
}
