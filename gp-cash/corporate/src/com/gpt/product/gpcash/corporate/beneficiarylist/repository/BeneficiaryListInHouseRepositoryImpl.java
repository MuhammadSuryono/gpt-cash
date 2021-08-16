package com.gpt.product.gpcash.corporate.beneficiarylist.repository;

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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;

public class BeneficiaryListInHouseRepositoryImpl extends CashRepositoryImpl<BeneficiaryListInHouseModel> {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "benAccountNo"));
	}
	
	public Page<BeneficiaryListInHouseModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BeneficiaryListInHouseModel> query = builder.createQuery(clazz);
		
		Root<BeneficiaryListInHouseModel> root = query.from(clazz);
		
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
		
		if (ValueUtils.hasValue(paramMap.get("isBenVA"))) {
			Predicate isVAPredicate = builder.equal(builder.upper(root.get("isBenVirtualAccount")), ((String)paramMap.get("isBenVA")).toUpperCase());
			if(predicate!=null)
				predicate = builder.and(predicate, isVAPredicate);
			else
				predicate = isVAPredicate;
		}

		Join<Object, Object> joinCorporate = root.join("corporate");
		joinCorporate.alias("corporate");
		Predicate corporateIsEqual = builder.equal(builder.upper(joinCorporate.get("id")), ((String)paramMap.get("corporateId")).toUpperCase());
		if(predicate!=null) {
			predicate = builder.and(predicate, corporateIsEqual);
		} else
			predicate = corporateIsEqual;
		
		if(paramMap.get("corporateUserGroupId") != null) {
			Join<Object, Object> joinUserGroup = root.join("corporateUserGroup");
			joinUserGroup.alias("corporateUserGroup");
			Predicate userGroupIsEqual = builder.equal(builder.upper(joinUserGroup.get("id")), ((String)paramMap.get("corporateUserGroupId")).toUpperCase());
			if(predicate!=null) {
				predicate = builder.and(predicate, userGroupIsEqual);
			} else
				predicate = userGroupIsEqual;
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
