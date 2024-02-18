package com.gpt.product.gpcash.retail.transaction.timedeposit.placement.repository;

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
import com.gpt.product.gpcash.retail.transaction.timedeposit.constants.CustomerTimeDepositConstants;
import com.gpt.product.gpcash.retail.transaction.timedeposit.placement.model.CustomerTimeDepositPlacementModel;

public class CustomerTimeDepositPlacementRepositoryImpl extends CashRepositoryImpl<CustomerTimeDepositPlacementModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "placementDate"));
	}

	public Page<CustomerTimeDepositPlacementModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerTimeDepositPlacementModel> query = builder.createQuery(clazz);

		Root<CustomerTimeDepositPlacementModel> root = query.from(clazz);

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

		Join<Object, Object> joinCustomer = root.join("customer");
		joinCustomer.alias("customer");
		Predicate customerIsEqual = builder.equal(builder.upper(joinCustomer.get("id")), ((String)paramMap.get("customerId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, customerIsEqual);
		} else
			predicate = customerIsEqual;
		
		Predicate isProcessed = builder.equal(builder.upper(root.get("isProcessed")), ApplicationConstants.YES);
		if(predicate!=null)
			predicate = builder.and(predicate, isProcessed);
		else
			predicate = isProcessed; 
		
		Predicate chequeReadyStatus = builder.equal(builder.upper(root.get("status")), CustomerTimeDepositConstants.STATUS_ACTIVE);
		if(predicate!=null)
			predicate = builder.and(predicate, chequeReadyStatus);
		else
			predicate = chequeReadyStatus;

		if (ValueUtils.hasValue(paramMap.get("timeDepositNo"))) {
			Predicate tdNo = builder.equal(builder.upper(root.get("timeDepositNo")), paramMap.get("timeDepositNo"));
			if (predicate !=null)
				predicate = builder.and(predicate, tdNo);
			else
				predicate = tdNo;
		}
		
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