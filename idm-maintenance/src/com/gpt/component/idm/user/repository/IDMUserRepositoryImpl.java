package com.gpt.component.idm.user.repository;

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
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class IDMUserRepositoryImpl extends CashRepositoryImpl<IDMUserModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "code"));
	}
	
	@Override
	public Page<IDMUserModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<IDMUserModel> query = builder.createQuery(IDMUserModel.class);
		
		Root<IDMUserModel> root = query.from(IDMUserModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("code"))) {
			predicate = builder.like(
							root.get("code"), "%" +  ((String)paramMap.get("code")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(
								          builder.upper(root.get("name")), "%" + ((String)paramMap.get("name")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("branchCode"))) {
			Join<Object, Object> joinBranch = root.join("branch");
			joinBranch.alias("branch");
			Predicate branchPredicate = builder.like(joinBranch.get("code"), (String)paramMap.get("branchCode"));
			if(predicate!=null)
				predicate = builder.and(predicate, branchPredicate);
			else
				predicate = branchPredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("stillLoginFlag"))) {
			Predicate stillLoginFlagPredicate = builder.equal(root.get("stillLoginFlag"), (String)paramMap.get("stillLoginFlag"));
			if(predicate!=null)
				predicate = builder.and(predicate, stillLoginFlagPredicate);
			else
				predicate = stillLoginFlagPredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("status"))) {
			Predicate statusPredicate = builder.equal(root.get("status"), (String)paramMap.get("status"));
			if(predicate!=null)
				predicate = builder.and(predicate, statusPredicate);
			else
				predicate = statusPredicate;
		}

		Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if(predicate!=null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted; 
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}

}

