package com.gpt.component.maintenance.wfrole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface WorkflowRoleRepository extends JpaRepository<WorkflowRoleModel, String>, CashRepository<WorkflowRoleModel>{
	
	@Query("select wfRole from WorkflowRoleModel wfRole where wfRole.application.code = ?1 order by idx, name")
	List<WorkflowRoleModel> findByApplicationCode(String applicationCode) throws Exception;
}