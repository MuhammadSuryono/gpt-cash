package com.gpt.product.gpcash.corporate.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.common.repository.BasicRepository;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;

@Repository
public interface CorporateProcessInstanceRepository extends JpaRepository<CorporateProcessInstance, String>, BasicRepository<CorporateProcessInstance> {

}
