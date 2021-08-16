package com.gpt.product.gpcash.corporate.pendingtaskuser.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;

public interface CorporateUserPendingTaskCustomRepository extends CashRepository<CorporateUserPendingTaskModel> {
	
	Page<CorporateUserPendingTaskModel> searchPendingTask(CorporateUserPendingTaskVO vo, Pageable pageInfo) throws Exception;

//	List<CorporateUserPendingTaskModel> getByServiceCodeAndInstructionDate(String serviceCode, Timestamp instructionDate) throws Exception;
	
}

