package com.gpt.component.maintenance.promo.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.promo.model.PromoModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class PromoRepositoryImpl extends CashRepositoryImpl<PromoModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "createdDate"));
	}
	
	@Override
	public Page<PromoModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PromoModel> query = builder.createQuery(PromoModel.class);
		
		Root<PromoModel> root = query.from(PromoModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("id"))) {
			predicate = builder.equal(root.get("id"), (String)paramMap.get("id"));
		}
		
		if (ValueUtils.hasValue(paramMap.get("infoType"))) {
			Predicate infoPredicate = builder.like( builder.upper(root.get("infoType")), "%" + (String)paramMap.get("infoType") + "%" );
			if(predicate!=null)
				predicate = builder.and(predicate, infoPredicate);
			else
				predicate = infoPredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("corporateId"))) {
			Predicate corpPredicate = builder.equal(
								          builder.upper(root.get("corpId")), ((String)paramMap.get("corporateId")).toUpperCase());
			if(predicate!=null)
				predicate = builder.and(predicate, corpPredicate);
			else
				predicate = corpPredicate;
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
