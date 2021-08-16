package com.gpt.product.gpcash.corporate.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;

@Repository
public interface CorporateVariableRepository extends JpaRepository<CorporateVariable, String> {

	CorporateVariable findByProcessInstanceAndName(CorporateProcessInstance pi, String name);
	
	CorporateVariable findByTaskInstanceAndName(CorporateTaskInstance ti, String name);
	
	void deleteByProcessInstanceAndName(CorporateProcessInstance pi, String name);
	
	void deleteByTaskInstanceAndName(CorporateTaskInstance ti, String name);
	
}
