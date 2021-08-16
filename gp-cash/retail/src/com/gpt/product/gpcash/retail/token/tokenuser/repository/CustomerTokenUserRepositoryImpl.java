package com.gpt.product.gpcash.retail.token.tokenuser.repository;

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
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;

public class CustomerTokenUserRepositoryImpl extends CashRepositoryImpl<CustomerTokenUserModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "tokenNo"));
	}
	
	@Override
	public Page<CustomerTokenUserModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerTokenUserModel> query = builder.createQuery(clazz);
		
		Root<CustomerTokenUserModel> root = query.from(clazz);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("tokenNo"))) {
			predicate = builder.equal(
							root.get("tokenNo"), ((String) paramMap.get("tokenNo")));
		}
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}
