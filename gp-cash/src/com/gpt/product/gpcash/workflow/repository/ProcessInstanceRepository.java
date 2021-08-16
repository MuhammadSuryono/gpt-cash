package com.gpt.product.gpcash.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.common.repository.BasicRepository;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, String>, BasicRepository<ProcessInstance> {

}
