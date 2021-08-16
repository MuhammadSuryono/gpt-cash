package com.gpt.product.gpcash.retail.transactionstatus.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;

public class CustomerTransactionStatusRepositoryImpl  extends CashRepositoryImpl<CustomerTransactionStatusModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.DESC, "activityDate"));
	}
}
