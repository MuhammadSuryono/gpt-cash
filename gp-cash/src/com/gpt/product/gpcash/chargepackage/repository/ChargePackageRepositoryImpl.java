package com.gpt.product.gpcash.chargepackage.repository;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
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
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;

public class ChargePackageRepositoryImpl extends CashRepositoryImpl<ChargePackageModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "code"));
	}
	
	@Override
	public Page<ChargePackageModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ChargePackageModel> query = builder.createQuery(ChargePackageModel.class);
		
		Root<ChargePackageModel> root = query.from(ChargePackageModel.class);

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
		
		if (ValueUtils.hasValue(paramMap.get(ApplicationConstants.APP_CODE))) {
			Join<Object, Object> joinApplication = root.join("application");
			joinApplication.alias("application");
			Predicate applicationPredicate = builder.equal(joinApplication.get("code"), (String)paramMap.get(ApplicationConstants.APP_CODE));
			if(predicate!=null)
				predicate = builder.and(predicate, applicationPredicate);
			else
				predicate = applicationPredicate;
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
	
	public void deleteChargePackageDetail(List<ChargePackageDetailModel> list) throws Exception {
		for(ChargePackageDetailModel detail : list){
			CriteriaBuilder builder = this.em.getCriteriaBuilder();
			CriteriaDelete<ChargePackageDetailModel> delete = builder.createCriteriaDelete(ChargePackageDetailModel.class);
			
			// set the root class
			Root<ChargePackageDetailModel> root = delete.from(ChargePackageDetailModel.class);
			
			// set where clause
			delete.where(builder.equal(root.get("id"), detail.getId()));
			
			// perform update
			this.em.createQuery(delete).executeUpdate();
		}
		
	}
}
