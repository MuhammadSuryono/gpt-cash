package com.gpt.product.gpcash.bankapprovalmatrix.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixModel;

public class BankApprovalMatrixRepositoryImpl extends CashRepositoryImpl<BankApprovalMatrixModel> {

	@Override
	protected Sort getDefaultSort() {
		return null;
	}
	
	@Override
	public Page<BankApprovalMatrixModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BankApprovalMatrixModel> query = builder.createQuery(clazz);

		Root<BankApprovalMatrixModel> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("id"))) {
			predicate = builder.equal(root.get("id"), paramMap.get("id"));
		}

		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(builder.upper(root.get("name")),
					"%" + ((String) paramMap.get("name")).toUpperCase() + "%");
			if (predicate != null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}

		if (ValueUtils.hasValue(paramMap.get("approvalMatrixMenuCode"))) {
			Join<Object, Object> joinMenu = root.join("menu");
			joinMenu.alias("menu");
			Predicate menuPredicate = builder.equal(joinMenu.get("code"), (String)paramMap.get("approvalMatrixMenuCode"));
			if(predicate!=null)
				predicate = builder.and(predicate, menuPredicate);
			else
				predicate = menuPredicate;
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
