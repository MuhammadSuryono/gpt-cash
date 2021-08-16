package com.gpt.product.gpcash.corporate.transaction.va.registration.repository;

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

import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationModel;

public class VARegistrationRepositoryImpl extends CashRepositoryImpl<VARegistrationModel> {
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "productCode"));
	}
	
	@Override
	public Page<VARegistrationModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<VARegistrationModel> query = builder.createQuery(clazz);

		Root<VARegistrationModel> root = query.from(clazz);

		Join<Object, Object> joinCorporate = root.join("corporate");
		joinCorporate.alias("corporate");
		Predicate corporateIsEqual = builder.equal(builder.upper(joinCorporate.get("id")),
				((String) paramMap.get("corporateId")).toUpperCase());
			
		query.where(corporateIsEqual);

		return createPageResult(pageInfo, builder, root, query, corporateIsEqual);
	}
}