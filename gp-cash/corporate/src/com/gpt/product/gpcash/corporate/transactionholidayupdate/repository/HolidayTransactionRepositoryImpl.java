package com.gpt.product.gpcash.corporate.transactionholidayupdate.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.model.HolidayTransactionModel;

public class HolidayTransactionRepositoryImpl  extends CashRepositoryImpl<HolidayTransactionModel> implements HolidayTransactionCustomRepository{
	
	@Override
	public Page<HolidayTransactionModel> findHolidayTransactions(Map<String, Object> map, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<HolidayTransactionModel> query = builder.createQuery(clazz);
		

		Root<HolidayTransactionModel> root = query.from(clazz);

		Predicate predicate = null;
		
		String referenceNo = (String)map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		predicate = builder.like(root.get("referenceNo"), "%" + referenceNo + "%");
		
		
		Predicate isProcessed = builder.equal(builder.upper(root.get("isProcessed")), ApplicationConstants.NO);
		if (predicate != null)
			predicate = builder.and(predicate, isProcessed);
		else
			predicate = isProcessed;

		
		if (predicate != null)
			query.where(predicate);
		
		
		return createPageResult(pageInfo, builder, root, query, predicate);

	}

}
