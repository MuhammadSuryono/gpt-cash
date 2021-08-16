package com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.repository;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.constants.TimeDepositConstants;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.model.TimeDepositPlacementModel;

public class TimeDepositPlacementRepositoryImpl extends CashRepositoryImpl<TimeDepositPlacementModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "placementDate"));
	}

	public Page<TimeDepositPlacementModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<TimeDepositPlacementModel> query = builder.createQuery(clazz);

		Root<TimeDepositPlacementModel> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("refNo"))) {
			predicate = builder.like(root.get("referenceNo"), (String)paramMap.get("refNo"));
		}
		
		if (ValueUtils.hasValue(paramMap.get("sourceAccount"))) {
			Join<Object, Object> joinSourceAccount = root.join("sourceAccount");
			joinSourceAccount.alias("sourceAccount");
			Predicate sourceAccountPredicate = builder.equal(joinSourceAccount.get("accountNo"), (String)paramMap.get("sourceAccount"));
			if(predicate!=null)
				predicate = builder.and(predicate, sourceAccountPredicate);
			else
				predicate = sourceAccountPredicate;
		}
		
		Join<Object, Object> joinCorp = root.join("corporate");
		joinCorp.alias("corporate");
		Predicate corpPredicate = builder.equal(joinCorp.get("id"), (String)paramMap.get("corporate"));
		if(predicate!=null)
			predicate = builder.and(predicate, corpPredicate);
		else
			predicate = corpPredicate;

		Predicate isProcessed = builder.equal(builder.upper(root.get("isProcessed")), ApplicationConstants.YES);
		if(predicate!=null)
			predicate = builder.and(predicate, isProcessed);
		else
			predicate = isProcessed; 
		
		Predicate chequeReadyStatus = builder.equal(builder.upper(root.get("status")), TimeDepositConstants.STATUS_ACTIVE);
		if(predicate!=null)
			predicate = builder.and(predicate, chequeReadyStatus);
		else
			predicate = chequeReadyStatus;

		/*Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if (predicate != null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted;*/

		if (predicate != null)
			query.where(predicate);

		return createPageResult(pageInfo, builder, root, query, predicate);
	}
	
	
}