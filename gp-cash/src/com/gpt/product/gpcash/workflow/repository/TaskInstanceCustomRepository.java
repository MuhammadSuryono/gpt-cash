package com.gpt.product.gpcash.workflow.repository;

import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.common.repository.BasicRepository;
import com.gpt.platform.cash.workflow.WFEngine;
import com.gpt.product.gpcash.workflow.model.TaskInstance;

public interface TaskInstanceCustomRepository extends BasicRepository<TaskInstance> {
	
	Page<Tuple> findActiveTasksByUser(Map<String, Object> map, WFEngine.Type type, Pageable pageInfo);
}
