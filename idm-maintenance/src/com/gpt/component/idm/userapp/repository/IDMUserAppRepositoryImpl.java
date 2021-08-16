package com.gpt.component.idm.userapp.repository;

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
import com.gpt.component.idm.userapp.model.IDMUserAppModel;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class IDMUserAppRepositoryImpl extends CashRepositoryImpl<IDMUserAppModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return null;
	}
	
	@Override
	public Page<IDMUserAppModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<IDMUserAppModel> query = builder.createQuery(IDMUserAppModel.class);
		
		Root<IDMUserAppModel> root = query.from(IDMUserAppModel.class);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("appCode"))) {
			Join<Object, Object> joinApp = root.join("application");
			joinApp.alias("application");
			predicate = builder.equal(builder.upper(joinApp.get("code")), ((String)paramMap.get("appCode")).toUpperCase());
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
