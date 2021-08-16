package com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;

public class TransactionStatusMappingRepositoryImpl  extends CashRepositoryImpl<TransactionStatusMappingModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "menuCode"));
	}
}