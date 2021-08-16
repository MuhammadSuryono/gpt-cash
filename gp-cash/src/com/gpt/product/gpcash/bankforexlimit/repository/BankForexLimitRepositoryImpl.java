package com.gpt.product.gpcash.bankforexlimit.repository;

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
import com.gpt.product.gpcash.bankforexlimit.model.BankForexLimitModel;

public class BankForexLimitRepositoryImpl extends CashRepositoryImpl<BankForexLimitModel> {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "currency"));
	}
	
	@Override
	public Page<BankForexLimitModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BankForexLimitModel> query = builder.createQuery(BankForexLimitModel.class);
		
		Root<BankForexLimitModel> root = query.from(BankForexLimitModel.class);
		
		Predicate predicate = null;
		
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
