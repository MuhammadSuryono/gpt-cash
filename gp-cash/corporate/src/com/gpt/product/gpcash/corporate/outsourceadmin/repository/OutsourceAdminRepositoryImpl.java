package com.gpt.product.gpcash.corporate.outsourceadmin.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;

public class OutsourceAdminRepositoryImpl extends CashRepositoryImpl<CorporateModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "id"));
	}

}
