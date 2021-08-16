package com.gpt.component.idm.menutree.repository;

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
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class IDMMenuTreeRepositoryImpl extends CashRepositoryImpl<IDMMenuTreeModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return null;
	}
	
	@Override
	public Page<IDMMenuTreeModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<IDMMenuTreeModel> query = builder.createQuery(IDMMenuTreeModel.class);
		
		Root<IDMMenuTreeModel> root = query.from(IDMMenuTreeModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("searchMenuCode"))) {
			Join<Object, Object> joinMenu = root.join("menu");
			joinMenu.alias("menu");
			predicate = builder.equal(joinMenu.get("code"), (String)paramMap.get("searchMenuCode"));
			
			Predicate isDeleted = builder.equal(joinMenu.get("deleteFlag"), ApplicationConstants.NO);
			predicate = builder.and(predicate, isDeleted);
		}
		
		if (ValueUtils.hasValue(paramMap.get("parentMenuCode"))) {
			Join<Object, Object> joinParentMenu = root.join("parentMenu");
			joinParentMenu.alias("parentMenu");
			Predicate parentMenuPredicate = builder.equal(joinParentMenu.get("code"), (String)paramMap.get("parentMenuCode"));
			if(predicate!=null)
				predicate = builder.and(predicate, parentMenuPredicate);
			else
				predicate = parentMenuPredicate;
			
			Predicate isDeleted = builder.equal(joinParentMenu.get("deleteFlag"), ApplicationConstants.NO);
			predicate = builder.and(predicate, isDeleted);
		}
		
		if (ValueUtils.hasValue(paramMap.get(ApplicationConstants.APP_CODE))) {
			Join<Object, Object> joinApplication = root.join("application");
			joinApplication.alias("application");
			Predicate applicationPredicate = builder.equal(joinApplication.get("code"), (String)paramMap.get(ApplicationConstants.APP_CODE));
			if(predicate!=null)
				predicate = builder.and(predicate, applicationPredicate);
			else
				predicate = applicationPredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("lvl"))) {
			Predicate levelPredicate = builder.equal(root.get("lvl"), ((String)paramMap.get("lvl")));
			if(predicate!=null)
				predicate = builder.and(predicate, levelPredicate);
			else
				predicate = levelPredicate;
		}
		
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}
