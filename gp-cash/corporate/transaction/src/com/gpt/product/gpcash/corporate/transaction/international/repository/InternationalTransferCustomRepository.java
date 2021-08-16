package com.gpt.product.gpcash.corporate.transaction.international.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.international.model.InternationalTransferModel;

public interface InternationalTransferCustomRepository extends CashRepository<InternationalTransferModel> {

	Page<InternationalTransferModel> searchForBank(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

}
