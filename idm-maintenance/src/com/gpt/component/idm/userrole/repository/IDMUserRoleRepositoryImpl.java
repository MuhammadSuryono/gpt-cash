package com.gpt.component.idm.userrole.repository;

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
import com.gpt.component.idm.userrole.model.IDMUserRoleModel;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class IDMUserRoleRepositoryImpl extends CashRepositoryImpl<IDMUserRoleModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return null;
	}
	
	@Override
	public Page<IDMUserRoleModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<IDMUserRoleModel> query = builder.createQuery(IDMUserRoleModel.class);
		
		Root<IDMUserRoleModel> root = query.from(IDMUserRoleModel.class);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("roleCode"))) {
			Join<Object, Object> joinRole = root.join("role");
			joinRole.alias("role");
			predicate = builder.equal(builder.upper(joinRole.get("code")), ((String)paramMap.get("roleCode")).toUpperCase());
		}
		
		if (ValueUtils.hasValue(paramMap.get("userCode"))) {
			Join<Object, Object> joinUser = root.join("user");
			joinUser.alias("user");
			Predicate idmUserIsEqual = builder.equal(builder.upper(joinUser.get("code")), ((String)paramMap.get("userCode")).toUpperCase());
			if(predicate!=null) {
				predicate = builder.and(predicate, idmUserIsEqual);
			} else
				predicate = idmUserIsEqual;
		}
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}

}
