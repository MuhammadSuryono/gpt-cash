package com.gpt.product.gpcash.retail.transactionstatusservicemapping.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.transactionstatusservicemapping.model.CustomerTransactionStatusMappingModel;

public class CustomerTransactionStatusMappingRepositoryImpl  extends CashRepositoryImpl<CustomerTransactionStatusMappingModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "menuCode"));
	}
}