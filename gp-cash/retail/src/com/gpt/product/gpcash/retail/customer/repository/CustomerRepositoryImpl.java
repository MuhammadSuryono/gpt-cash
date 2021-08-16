package com.gpt.product.gpcash.retail.customer.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

public class CustomerRepositoryImpl extends CashRepositoryImpl<CustomerModel>  {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "id"));
	}
	
	public Page<CustomerModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerModel> query = builder.createQuery(clazz);

		Root<CustomerModel> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get(ApplicationConstants.USER_ID))) {
			predicate = builder.like(root.get("userId"), "%" + paramMap.get(ApplicationConstants.USER_ID) + "%");
		}

		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(builder.upper(root.get("name")),
					"%" + ((String) paramMap.get("name")).toUpperCase() + "%");
			if (predicate != null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}

		Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if (predicate != null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted;

		if (predicate != null)
			query.where(predicate);

		return createPageResult(pageInfo, builder, root, query, predicate);
	}
	
}
