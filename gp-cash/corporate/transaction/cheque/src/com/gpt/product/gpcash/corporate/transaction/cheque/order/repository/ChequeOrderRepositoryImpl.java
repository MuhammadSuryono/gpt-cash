package com.gpt.product.gpcash.corporate.transaction.cheque.order.repository;

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
import com.gpt.product.gpcash.corporate.transaction.cheque.constants.ChequeConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderModel;

public class ChequeOrderRepositoryImpl extends CashRepositoryImpl<ChequeOrderModel> implements ChequeOrderCustomRepository {
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "orderNo"));
	}
	
	@Override
	public Page<ChequeOrderModel> searchForCorporate(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ChequeOrderModel> query = builder.createQuery(ChequeOrderModel.class);
		
		Root<ChequeOrderModel> root = query.from(ChequeOrderModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("orderNo"))) {
			predicate = builder.like(
							root.get("orderNo"), "%" +  ((String)paramMap.get("orderNo")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("referenceNo"))) {
			Predicate namePredicate = builder.like(
								          builder.upper(root.get("referenceNo")), "%" + ((String)paramMap.get("referenceNo")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
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
		
		Join<Object, Object> joinUserGroup = root.join("corporateUserGroup");
		joinUserGroup.alias("corporateUserGroup");
		Predicate userGroupPredicate = builder.equal(joinUserGroup.get("id"), (String)paramMap.get("userGroupId"));
		if(predicate!=null)
			predicate = builder.and(predicate, userGroupPredicate);
		else
			predicate = userGroupPredicate;
		
		Predicate isProcessed = builder.equal(builder.upper(root.get("isProcessed")), ApplicationConstants.YES);
		if(predicate!=null)
			predicate = builder.and(predicate, isProcessed);
		else
			predicate = isProcessed; 
		
		Predicate chequeReadyStatus = builder.equal(builder.upper(root.get("status")), ChequeConstants.CHQ_STS_PICKED_UP);
		if(predicate!=null)
			predicate = builder.and(predicate, chequeReadyStatus);
		else
			predicate = chequeReadyStatus;
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
	
	@Override
	public Page<ChequeOrderModel> searchForBank(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ChequeOrderModel> query = builder.createQuery(ChequeOrderModel.class);
		
		Root<ChequeOrderModel> root = query.from(ChequeOrderModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("orderNo"))) {
			predicate = builder.like(
							root.get("orderNo"), "%" +  ((String)paramMap.get("orderNo")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("statusCode"))) {
			Predicate statusPredicate = builder.equal(
								          builder.upper(root.get("status")), (String)paramMap.get("statusCode"));
			if(predicate!=null)
				predicate = builder.and(predicate, statusPredicate);
			else
				predicate = statusPredicate;
		}
		
		Join<Object, Object> joinBranch = root.join("branch");
		joinBranch.alias("branch");
		Predicate branchPredicate = builder.equal(joinBranch.get("code"), (String)paramMap.get("branchCode"));
		if(predicate!=null)
			predicate = builder.and(predicate, branchPredicate);
		else
			predicate = branchPredicate; 
		
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}