package com.gpt.product.gpcash.corporate.beneficiarylist.repository;

import java.util.Map;

import javax.persistence.NoResultException;
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
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;

public class BeneficiaryListInternationalRepositoryImpl extends CashRepositoryImpl<BeneficiaryListInternationalModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "benAccountNo"));
	}
	
	public Page<BeneficiaryListInternationalModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BeneficiaryListInternationalModel> query = builder.createQuery(clazz);
		
		Root<BeneficiaryListInternationalModel> root = query.from(clazz);
		
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
		
		if (ValueUtils.hasValue(paramMap.get("bankCountryCode"))) {
			
			Join<Object, Object> joinCountry = root.join("benInternationalCountry");
			joinCountry.alias("benInternationalCountry");
			Predicate countryPredicate = builder.equal(builder.upper(joinCountry.get("code")), (String)paramMap.get("bankCountryCode"));
			if(predicate!=null) {
				predicate = builder.and(predicate, countryPredicate);
			} else
				predicate = countryPredicate;
			
			/*Join<Object, Object> joinBank = root.join("benInternationalBankCode");
			joinBank.alias("benInternationalBankCode");
			
			CriteriaQuery<InternationalBankModel> query2 = builder.createQuery(InternationalBankModel.class);
			Root<InternationalBankModel> bankModel = query2.from(InternationalBankModel.class);
			Join<Object, Object> joinCountry = bankModel.join("country");
			joinCountry.alias("country");
			
			query2.select(bankModel.get("code"));
			query2.where(builder.equal(builder.upper(joinCountry.get("code")), paramMap.get("bankCountryCode")));
			
			try {
				
				Predicate bankPredicate = builder.equal(builder.upper(joinBank.get("code")), em.createQuery(query2).getSingleResult());
				
				if(predicate!=null) {
					predicate = builder.and(predicate, bankPredicate);
				} else
					predicate = bankPredicate;
			} catch (NoResultException e) {
				
			}*/
			
		}
		
		if (ValueUtils.hasValue(paramMap.get("bankCode"))) {
			Join<Object, Object> joinBank = root.join("benInternationalBankCode");
			joinBank.alias("benInternationalBankCode");
			Predicate bankPredicate = builder.equal(builder.upper(joinBank.get("code")), (String)paramMap.get("bankCode"));
			if(predicate!=null) {
				predicate = builder.and(predicate, bankPredicate);
			} else
				predicate = bankPredicate;
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

