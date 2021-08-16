package com.gpt.product.gpcash.corporate.transaction.purchasepayee.repository;

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
import com.gpt.product.gpcash.corporate.transaction.purchasepayee.model.PurchasePayeeModel;

public class PurchasePayeeRepositoryImpl extends CashRepositoryImpl<PurchasePayeeModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "payeeName"));
	}
	
	public Page<PurchasePayeeModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PurchasePayeeModel> query = builder.createQuery(clazz);
		
		Root<PurchasePayeeModel> root = query.from(clazz);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("payeeName"))) {
			predicate = builder.like(
							root.get("payeeName"), "%" +  ((String)paramMap.get("payeeName")) + "%");
		}
		
		Join<Object, Object> joinCorporate = root.join("corporate");
		joinCorporate.alias("corporate");
		Predicate corporateIsEqual = builder.equal(builder.upper(joinCorporate.get("id")), ((String)paramMap.get("corporateId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, corporateIsEqual);
		} else
			predicate = corporateIsEqual;
		
		Join<Object, Object> joinUserGroup = root.join("corporateUserGroup");
		joinUserGroup.alias("corporateUserGroup");
		Predicate userGroupIsEqual = builder.equal(builder.upper(joinUserGroup.get("id")), ((String)paramMap.get("corporateUserGroupId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, userGroupIsEqual);
		} else
			predicate = userGroupIsEqual;

		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}
