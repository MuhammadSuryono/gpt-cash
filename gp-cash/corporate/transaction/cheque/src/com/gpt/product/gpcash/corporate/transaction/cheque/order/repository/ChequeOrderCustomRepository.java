package com.gpt.product.gpcash.corporate.transaction.cheque.order.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderModel;

public interface ChequeOrderCustomRepository extends CashRepository<ChequeOrderModel> {

	Page<ChequeOrderModel> searchForCorporate(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

	Page<ChequeOrderModel> searchForBank(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

}
