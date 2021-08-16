package com.gpt.component.maintenance.interestrate.repository;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.interestrate.model.InterestRateModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class InterestRateRepositoryImpl extends CashRepositoryImpl<InterestRateModel> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "productCode"));
	}

	@Override
	public Page<InterestRateModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<InterestRateModel> query = builder.createQuery(InterestRateModel.class);
		
		Root<InterestRateModel> root = query.from(InterestRateModel.class);
		root.alias("ir");
		
		Predicate predicate = null;
		
		if (ValueUtils.hasValue(paramMap.get("productCode"))) {
			predicate = builder.like(root.get("productCode"), "%" +  ((String)paramMap.get("productCode")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("productName"))) {
			Predicate namePredicate = builder.like(builder.upper(root.get("productName")), "%" + ((String)paramMap.get("productName")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}
		
		Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if(predicate!=null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted;
		
		if(predicate!=null)
			query.where(predicate);
		
//		TypedQuery<InterestRateModel> findModel = em.createQuery(query);
//		System.out.println("===========Query1: ======================= " + findModel.unwrap(org.hibernate.Query.class).getQueryString());
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
	
}
