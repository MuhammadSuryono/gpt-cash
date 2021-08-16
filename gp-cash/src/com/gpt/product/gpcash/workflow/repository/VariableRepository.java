package com.gpt.product.gpcash.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.workflow.model.ProcessInstance;
import com.gpt.product.gpcash.workflow.model.TaskInstance;
import com.gpt.product.gpcash.workflow.model.Variable;

@Repository
public interface VariableRepository extends JpaRepository<Variable, String> {

	Variable findByProcessInstanceAndName(ProcessInstance pi, String name);
	
	Variable findByTaskInstanceAndName(TaskInstance ti, String name);
	
	void deleteByProcessInstanceAndName(ProcessInstance pi, String name);
	
	void deleteByTaskInstanceAndName(TaskInstance ti, String name);
	
}
