package com.gpt.product.gpcash.corporate.corporateusergroup.repository;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;

public class CorporateUserGroupRepositoryImpl extends CashRepositoryImpl<CorporateUserGroupModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "code"));
	}
	
	@Override
	public Page<CorporateUserGroupModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateUserGroupModel> query = builder.createQuery(clazz);

		Root<CorporateUserGroupModel> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("code"))) {
			predicate = builder.like(root.get("code"), "%" + ((String) paramMap.get("code")) + "%");
		}

		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(builder.upper(root.get("name")),
					"%" + ((String) paramMap.get("name")).toUpperCase() + "%");
			if (predicate != null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}

		if (ValueUtils.hasValue(paramMap.get("corporateId"))) {
			Join<Object, Object> joinCorporate = root.join("corporate");
			joinCorporate.alias("corporate");
			Predicate corporateIsEqual = builder.equal(builder.upper(joinCorporate.get("id")),
					((String) paramMap.get("corporateId")).toUpperCase());
			if (predicate != null) {
				predicate = builder.and(predicate, corporateIsEqual);
			} else
				predicate = corporateIsEqual;
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
	
	public Page<CorporateUserGroupModel> searchUserGroupNotAdmin(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateUserGroupModel> query = builder.createQuery(clazz);

		Root<CorporateUserGroupModel> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("code"))) {
			predicate = builder.like(root.get("code"), "%" + ((String) paramMap.get("code")) + "%");
		}

		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(builder.upper(root.get("name")),
					"%" + ((String) paramMap.get("name")).toUpperCase() + "%");
			if (predicate != null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}

		if (ValueUtils.hasValue(paramMap.get("corporateId"))) {
			Join<Object, Object> joinCorporate = root.join("corporate");
			joinCorporate.alias("corporate");
			Predicate corporateIsEqual = builder.equal(builder.upper(joinCorporate.get("id")),
					((String) paramMap.get("corporateId")).toUpperCase());
			if (predicate != null) {
				predicate = builder.and(predicate, corporateIsEqual);
			} else
				predicate = corporateIsEqual;
		}

		Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if (predicate != null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted;
		
		Join<Object, Object> joinRole = root.join("role");
		joinRole.alias("role");
		Predicate isRoleNotEqual = builder.notEqual(builder.upper(joinRole.get("code")), ApplicationConstants.ROLE_AP_ADMIN);
		if (predicate != null)
			predicate = builder.and(predicate, isRoleNotEqual);
		else
			predicate = isRoleNotEqual;

		if (predicate != null)
			query.where(predicate);

		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}