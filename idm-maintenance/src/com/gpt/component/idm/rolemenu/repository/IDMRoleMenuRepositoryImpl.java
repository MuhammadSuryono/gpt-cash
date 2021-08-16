package com.gpt.component.idm.rolemenu.repository;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;

import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class IDMRoleMenuRepositoryImpl extends CashRepositoryImpl<IDMRoleMenuModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return null;
	}
	
	public List<IDMRoleMenuModel> searchRoleMenu(String roleCode) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<IDMRoleMenuModel> query = builder.createQuery(IDMRoleMenuModel.class);
		
		Root<IDMRoleMenuModel> root = query.from(IDMRoleMenuModel.class);
		
		Predicate predicate = null;
		Join<Object, Object> joinRole = root.join("role");
		joinRole.alias("role");
		predicate = builder.equal(builder.upper(joinRole.get("code")), roleCode.toUpperCase());
		
		if(predicate!=null)
			query.where(predicate);
		
		return em.createQuery(query).getResultList();
	}

}

