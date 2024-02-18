package com.gpt.product.gpcash.retail.transaction.purchasepayee.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.transaction.purchasepayee.model.CustomerPurchasePayeeModel;

public class CustomerPurchasePayeeRepositoryImpl extends CashRepositoryImpl<CustomerPurchasePayeeModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "payeeName"));
	}
	
	public Page<CustomerPurchasePayeeModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerPurchasePayeeModel> query = builder.createQuery(clazz);
		
		Root<CustomerPurchasePayeeModel> root = query.from(clazz);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("payeeName"))) {
			predicate = builder.like(
							root.get("payeeName"), "%" +  ((String)paramMap.get("payeeName")) + "%");
		}
		
		Join<Object, Object> joinCustomer = root.join("customer");
		joinCustomer.alias("customer");
		Predicate customerIsEqual = builder.equal(builder.upper(joinCustomer.get("id")), ((String)paramMap.get("customerId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, customerIsEqual);
		} else
			predicate = customerIsEqual;

		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}
