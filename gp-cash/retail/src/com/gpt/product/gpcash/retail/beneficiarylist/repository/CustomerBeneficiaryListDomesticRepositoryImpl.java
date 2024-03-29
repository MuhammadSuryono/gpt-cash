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
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;

public class CustomerBeneficiaryListDomesticRepositoryImpl extends CashRepositoryImpl<CustomerBeneficiaryListDomesticModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "benAccountNo"));
	}
	
	public Page<CustomerBeneficiaryListDomesticModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerBeneficiaryListDomesticModel> query = builder.createQuery(clazz);
		
		Root<CustomerBeneficiaryListDomesticModel> root = query.from(clazz);
		
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
		
		if (ValueUtils.hasValue(paramMap.get("bankCode"))) {
			Join<Object, Object> joinBank = root.join("benDomesticBankCode");
			joinBank.alias("benDomesticBankCode");
			Predicate bankPredicate = builder.equal(builder.upper(joinBank.get("code")), (String)paramMap.get("bankCode"));
			if(predicate!=null) {
				predicate = builder.and(predicate, bankPredicate);
			} else
				predicate = bankPredicate;
		}
		
		Join<Object, Object> joinCustomer = root.join("customer");
		joinCustomer.alias("customer");
		Predicate customerIsEqual = builder.equal(builder.upper(joinCustomer.get("id")), ((String)paramMap.get("customerId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, customerIsEqual);
		} else
			predicate = customerIsEqual;
		
		if (ValueUtils.hasValue(paramMap.get("isOnline"))) {
			Predicate isOnline = builder.equal(builder.upper(root.get("isBenOnline")), (String) paramMap.get("isOnline"));
			if(predicate!=null)
				predicate = builder.and(predicate, isOnline);
			else
				predicate = isOnline;
		} else { // exclude Online
//			Predicate isOnline = builder.isNull(root.get("isBenOnline"));
			Predicate isOnline = builder.equal(builder.upper(root.get("isBenOnline")), ApplicationConstants.NO);
			if(predicate!=null)
				predicate = builder.and(predicate, isOnline);
			else
				predicate = isOnline;
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

