package com.gpt.product.gpcash.approvalmap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.approvalmap.model.ApprovalMapModel;

@Repository
public interface ApprovalMapRepository extends JpaRepository<ApprovalMapModel, String>, CashRepository<ApprovalMapModel>{

	List<ApprovalMapModel> findByWorkflowRoleCodeAndApprovalLevelCode(String wfRoleCode, String approvalLevel) throws Exception;
		
}
