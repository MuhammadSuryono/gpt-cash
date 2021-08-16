package com.gpt.product.gpcash.biller.purchaseinstitution.repository;

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
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;

public class PurchaseInstitutionRepositoryImpl extends CashRepositoryImpl<PurchaseInstitutionModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "code"));
	}
	
	@Override
	public Page<PurchaseInstitutionModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PurchaseInstitutionModel> query = builder.createQuery(clazz);
		
		Root<PurchaseInstitutionModel> root = query.from(clazz);
		
		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("code"))) {
			predicate = builder.like(
							root.get("code"), "%" +  ((String)paramMap.get("code")) + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(
								          builder.upper(root.get("name")), "%" + ((String)paramMap.get("name")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("purchaseInstitutionCategoryCode"))) {
			Join<Object, Object> joinPurchaseInstitutionCategory = root.join("purchaseInstitutionCategory");
			joinPurchaseInstitutionCategory.alias("purchaseInstitutionCategory");
			Predicate purchaseInstitutionCategoryIsEqual = builder.equal(builder.upper(joinPurchaseInstitutionCategory.get("code")), ((String)paramMap.get("purchaseInstitutionCategoryCode")).toUpperCase());
			if(predicate!=null) {
				predicate = builder.and(predicate, purchaseInstitutionCategoryIsEqual);
			} else
				predicate = purchaseInstitutionCategoryIsEqual;
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
