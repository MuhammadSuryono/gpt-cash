package com.gpt.product.gpcash.retail.pendingtaskuser.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;

public interface CustomerUserPendingTaskCustomRepository extends CashRepository<CustomerUserPendingTaskModel> {
	
	Page<CustomerUserPendingTaskModel> searchPendingTask(Map<String, Object> map, Pageable pageInfo);

}

