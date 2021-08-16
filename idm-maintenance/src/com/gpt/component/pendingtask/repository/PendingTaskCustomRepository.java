package com.gpt.component.pendingtask.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.pendingtask.model.PendingTaskModel;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.repository.CashRepository;

public interface PendingTaskCustomRepository extends CashRepository<PendingTaskModel> {
	
	Page<PendingTaskModel> searchPendingTask(PendingTaskVO vo, Pageable pageInfo) throws Exception;
	
}
