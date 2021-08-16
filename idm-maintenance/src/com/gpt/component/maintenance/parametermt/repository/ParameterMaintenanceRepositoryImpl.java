package com.gpt.component.maintenance.parametermt.repository;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.parametermt.model.ParameterMaintenanceModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class ParameterMaintenanceRepositoryImpl extends CashRepositoryImpl<ParameterMaintenanceModel> implements ParameterMaintenanceCustomRepository {
	
	@Autowired
	private ParameterMaintenanceRepository repo;
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "idx"), new Order(Direction.ASC, "name"));
	}
	
	@Override
	public Page<Object> searchModel(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		String modelCode = (String)paramMap.get("modelCode");
		if(logger.isDebugEnabled()) {
			logger.debug("modelCode : " + modelCode);
		}
		
		ParameterMaintenanceModel parameter = repo.findOne(modelCode);
		
		@SuppressWarnings("unchecked")
		Class<Object> clazz = (Class<Object>)Class.forName(parameter.getModelName());
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Object> query = builder.createQuery(clazz);
		
		Root<Object> root = query.from(clazz);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("code"))) {
			predicate = builder.like(
							builder.upper(root.get("code")), "%" +  ((String)paramMap.get("code")).toUpperCase() + "%");
		}
		
		if (ValueUtils.hasValue(paramMap.get("name"))) {
			Predicate namePredicate = builder.like(
								          builder.upper(root.get("name")), "%" + ((String)paramMap.get("name")).toUpperCase() + "%");
			if(predicate!=null)
				predicate = builder.and(predicate, namePredicate);
			else
				predicate = namePredicate;
		}
		
		if (ValueUtils.hasValue(paramMap.get("parentProperty"))) {
			String parentProperty = (String) paramMap.get("parentProperty");
			Join<Object, Object> joinParentProperty = root.join(parentProperty);
			joinParentProperty.alias(parentProperty);
			Predicate joinParentPropertyIsLike = builder.like(builder.upper(joinParentProperty.get("code")),
					((String) paramMap.get("parentPropertySearchCode")).toUpperCase());
			if (predicate != null) {
				predicate = builder.and(predicate, joinParentPropertyIsLike);
			} else
				predicate = joinParentPropertyIsLike;
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
	
	@Override
	public Object findAnyOne(Class<?> clazz, String code) {
		return em.find(clazz, code);
	}
	
	@Override
	public void saveAnyOne(Object model) {
		em.persist(model);
	}
	
	@Override
	public void updateAnyOne(Object model) {
		em.merge(model);
	}
	
	@Override
	public Object getParameterMaintenanceByModelAndName(String modelCode, String name) throws ClassNotFoundException {
		ParameterMaintenanceModel parameter = repo.findOne(modelCode);
		
		@SuppressWarnings("unchecked")
		Class<Object> clazz = (Class<Object>) Class.forName(parameter.getModelName());
		clazz.getName();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Object> query = builder.createQuery(clazz);
		
		Root<Object> root = query.from(clazz);
		Predicate predicate = null;
		if (ValueUtils.hasValue(name)) {
			Predicate namePredicate = builder.like(builder.upper(root.get("name")), "%" + (name).toUpperCase() + "%");
			predicate = namePredicate;
		}
		
		if(predicate!=null)
			query.where(predicate);
		
		List<Object> resultLIst = this.em.createQuery(query).getResultList();
		
		if (resultLIst!= null && !resultLIst.isEmpty()) {
			return resultLIst.get(0);
		}
		
		return new Object();
	}
}
