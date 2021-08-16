package com.gpt.product.gpcash.corporate.corporatetdspecialrate.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporatetdspecialrate.model.TDSpecialRateModel;

public class TDSpecialRateRepositoryImpl extends CashRepositoryImpl<TDSpecialRateModel> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "expiryDate"));
	}

	@Override
	public Page<TDSpecialRateModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<TDSpecialRateModel> query = builder.createQuery(TDSpecialRateModel.class);
		
		Root<TDSpecialRateModel> root = query.from(TDSpecialRateModel.class);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("dateFrom")) && ValueUtils.hasValue(paramMap.get("dateTo"))) {
			Calendar calFrom = DateUtils.getEarliestDate((Date)paramMap.get("dateFrom"));
			Calendar calTo = DateUtils.getNextEarliestDate((Date)paramMap.get("dateTo"));
			
			predicate = builder.and(
							builder.greaterThanOrEqualTo(root.get("expiryDate"), calFrom.getTime()),
							builder.lessThan(root.get("expiryDate"), calTo.getTime())
						);
		}
		
	
		
		if (ValueUtils.hasValue(paramMap.get("refNoSpecialRate"))) {
			Predicate refNoLike = builder.like(
							root.get("refNoSpecialRate"),   ((String)paramMap.get("refNoSpecialRate")));
			if (predicate != null)
				predicate = builder.and(predicate, refNoLike);
			else
				predicate = refNoLike;
		}
		
		if (ValueUtils.hasValue(paramMap.get("corporateId"))) {
			Predicate corporateCodeIsEqual = builder.equal(builder.upper(root.join("corporate").get("id")), ((String)paramMap.get("corporateId")).toUpperCase());
			if (predicate != null)
				predicate = builder.and(predicate, corporateCodeIsEqual);
			else
				predicate = corporateCodeIsEqual;
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
