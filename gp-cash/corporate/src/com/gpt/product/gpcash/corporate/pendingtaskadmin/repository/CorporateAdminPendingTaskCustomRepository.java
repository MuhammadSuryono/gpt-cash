package com.gpt.product.gpcash.corporate.pendingtaskadmin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

public interface CorporateAdminPendingTaskCustomRepository extends CashRepository<CorporateAdminPendingTaskModel> {
	
	Page<CorporateAdminPendingTaskModel> searchPendingTask(CorporateAdminPendingTaskVO vo, Pageable pageInfo) throws Exception;
	
}
