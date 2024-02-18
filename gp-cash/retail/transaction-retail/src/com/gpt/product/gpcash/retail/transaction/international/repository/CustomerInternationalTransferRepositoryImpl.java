package com.gpt.product.gpcash.retail.transaction.international.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.retail.transaction.international.model.CustomerInternationalTransferModel;

public class CustomerInternationalTransferRepositoryImpl extends CashRepositoryImpl<CustomerInternationalTransferModel> implements CustomerInternationalTransferCustomRepository {

	@Override
	public Page<CustomerInternationalTransferModel> searchForBank(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerInternationalTransferModel> query = builder.createQuery(CustomerInternationalTransferModel.class);
		
		Root<CustomerInternationalTransferModel> root = query.from(CustomerInternationalTransferModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("referenceNo"))) {
			predicate = builder.like( root.get("referenceNo"), "%" +  ((String)paramMap.get("referenceNo")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("statusCode"))) {
			Predicate statusPredicate = builder.equal(builder.upper(root.get("internationalTransferStatus")), (String)paramMap.get("statusCode"));
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
