package com.gpt.product.gpcash.menumapping.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.menumapping.model.MenuMappingModel;

public class MenuMappingRepositoryImpl  extends CashRepositoryImpl<MenuMappingModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "menuCode"));
	}
}