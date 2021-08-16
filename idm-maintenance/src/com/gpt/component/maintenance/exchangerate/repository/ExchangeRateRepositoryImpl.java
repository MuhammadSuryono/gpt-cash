package com.gpt.component.maintenance.exchangerate.repository;

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
import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class ExchangeRateRepositoryImpl extends CashRepositoryImpl<ExchangeRateModel> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.DESC, "effectiveDate"));
	}

	@Override
	public Page<ExchangeRateModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ExchangeRateModel> query = builder.createQuery(ExchangeRateModel.class);
		
		Root<ExchangeRateModel> root = query.from(ExchangeRateModel.class);
		
		Predicate predicate = null;
		/*if (ValueUtils.hasValue(paramMap.get("fromDate")) && ValueUtils.hasValue(paramMap.get("toDate"))) {
			Calendar calFrom = DateUtils.getEarliestDate((Date)paramMap.get("fromDate"));
			Calendar calTo = DateUtils.getNextEarliestDate((Date)paramMap.get("toDate"));
			
			predicate = builder.and(
							builder.greaterThanOrEqualTo(root.get("effectiveDate"), calFrom.getTime()),
							builder.lessThan(root.get("effectiveDate"), calTo.getTime())
						);
		}*/
		
		if (ValueUtils.hasValue(paramMap.get("currencyCode"))) {
			Predicate currCodeIsEqual = builder.equal(builder.upper(root.join("currency").get("code")), ((String)paramMap.get("currencyCode")).toUpperCase());
			/*if(predicate!=null) {
				predicate = builder.and(predicate, currCodeIsEqual);
			} else*/
				predicate = currCodeIsEqual;
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
