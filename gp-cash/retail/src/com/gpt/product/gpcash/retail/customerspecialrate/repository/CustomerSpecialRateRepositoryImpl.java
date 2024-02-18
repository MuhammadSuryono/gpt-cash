package com.gpt.product.gpcash.retail.customerspecialrate.repository;

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
import com.gpt.product.gpcash.retail.customerspecialrate.model.CustomerSpecialRateModel;

public class CustomerSpecialRateRepositoryImpl extends CashRepositoryImpl<CustomerSpecialRateModel> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "expiryDate"));
	}

	@Override
	public Page<CustomerSpecialRateModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerSpecialRateModel> query = builder.createQuery(CustomerSpecialRateModel.class);
		
		Root<CustomerSpecialRateModel> root = query.from(CustomerSpecialRateModel.class);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("dateFrom")) && ValueUtils.hasValue(paramMap.get("dateTo"))) {
			Calendar calFrom = DateUtils.getEarliestDate((Date)paramMap.get("dateFrom"));
			Calendar calTo = DateUtils.getNextEarliestDate((Date)paramMap.get("dateTo"));
			
			predicate = builder.and(
							builder.greaterThanOrEqualTo(root.get("expiryDate"), calFrom.getTime()),
							builder.lessThan(root.get("expiryDate"), calTo.getTime())
						);
		}
		
		if (ValueUtils.hasValue(paramMap.get("foreignCurrency1"))) {
			Predicate currCodeIsEqual = builder.equal(builder.upper(root.join("foreignCurrency1").get("code")), ((String)paramMap.get("foreignCurrency1")).toUpperCase());
			if (predicate != null)
				predicate = builder.and(predicate, currCodeIsEqual);
			else
				predicate = currCodeIsEqual;
		}
		
		if (ValueUtils.hasValue(paramMap.get("foreignCurrency2"))) {
			Predicate currCodeIsEqual = builder.equal(builder.upper(root.join("foreignCurrency2").get("code")), ((String)paramMap.get("foreignCurrency2")).toUpperCase());
			if (predicate != null)
				predicate = builder.and(predicate, currCodeIsEqual);
			else
				predicate = currCodeIsEqual;
		}
		
		if (ValueUtils.hasValue(paramMap.get("refNoSpecialRate"))) {
			Predicate refNoLike = builder.like(
							root.get("refNoSpecialRate"),   ((String)paramMap.get("refNoSpecialRate")));
			if (predicate != null)
				predicate = builder.and(predicate, refNoLike);
			else
				predicate = refNoLike;
		}
		
		if (ValueUtils.hasValue(paramMap.get("customerId"))) {
			Predicate customerCodeIsEqual = builder.equal(builder.upper(root.join("customer").get("id")), ((String)paramMap.get("customerId")).toUpperCase());
			if (predicate != null)
				predicate = builder.and(predicate, customerCodeIsEqual);
			else
				predicate = customerCodeIsEqual;
		}
		
		if (ValueUtils.hasValue(paramMap.get("rateType"))) {
			Predicate rateTypeisEqual = builder.equal(builder.upper(root.get("rateType")),(String)paramMap.get("rateType"));
			if (predicate != null)
				predicate = builder.and(predicate, rateTypeisEqual);
			else
				predicate = rateTypeisEqual;
		}
		
		if (ValueUtils.hasValue(paramMap.get("status"))) {
			Predicate statusisEqual = builder.equal(builder.upper(root.get("status")),(String)paramMap.get("status"));
			if (predicate != null)
				predicate = builder.and(predicate, statusisEqual);
			else
				predicate = statusisEqual;
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
