package com.gpt.product.gpcash.corporate.workflow.repository;

import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.common.repository.BasicRepository;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;

public interface CorporateTaskInstanceCustomRepository extends BasicRepository<CorporateTaskInstance> {
	
	Page<Tuple> findActiveTasksByUser(Map<String, Object> map, CorporateWFEngine.Type type, Pageable pageInfo);

	/**
	 * This method can only work properly if for each pending task there is always at minimum a task instance assigned to a user
	 * This is because we join directly with task instance to get the result for better performance.
	 * 
	 * @param map
	 * @param pageInfo
	 * @return
	 */
	Page<CorporateUserPendingTaskModel> findPendingTasks(Map<String, Object> map, Pageable pageInfo);

	Page<CorporateAdminPendingTaskModel> findNonFinancialPendingTasks(Map<String, Object> map, Pageable pageInfo);

}
