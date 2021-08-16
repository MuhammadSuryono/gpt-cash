package com.gpt.product.gpcash.retail.beneficiarylist.repository;

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
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInHouseModel;

public class CustomerBeneficiaryListInHouseRepositoryImpl extends CashRepositoryImpl<CustomerBeneficiaryListInHouseModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "benAccountNo"));
	}
	
	public Page<CustomerBeneficiaryListInHouseModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerBeneficiaryListInHouseModel> query = builder.createQuery(clazz);
		
		Root<CustomerBeneficiaryListInHouseModel> root = query.from(clazz);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("benAccountNo"))) {
			predicate = builder.like(
							root.get("benAccountNo"), "%" +  ((String)paramMap.get("benAccountNo")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("benAccountName"))) {
			Predicate namePredicate = builder.like(
								          builder.upper(root.get("benAccountName")), "%" + ((String)paramMap.get("benAccountName")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}

		Join<Object, Object> joinCustomer = root.join("customer");
		joinCustomer.alias("customer");
		Predicate customerIsEqual = builder.equal(builder.upper(joinCustomer.get("id")), ((String)paramMap.get("customerId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, customerIsEqual);
		} else
			predicate = customerIsEqual;
		
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
