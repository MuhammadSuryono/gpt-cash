package com.gpt.product.gpcash.retail.transaction.international.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.international.model.CustomerInternationalTransferModel;

public interface CustomerInternationalTransferCustomRepository extends CashRepository<CustomerInternationalTransferModel> {

	Page<CustomerInternationalTransferModel> searchForBank(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

}
