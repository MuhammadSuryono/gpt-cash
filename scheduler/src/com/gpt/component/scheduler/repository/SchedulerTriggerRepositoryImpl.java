package com.gpt.component.scheduler.repository;

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
import com.gpt.component.scheduler.model.SchedulerTriggerModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;

public class SchedulerTriggerRepositoryImpl extends CashRepositoryImpl<SchedulerTriggerModel> {
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "schedulerTask.code"));
	}
	
	@Override
	public Page<SchedulerTriggerModel> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SchedulerTriggerModel> query = builder.createQuery(clazz);
		
		Root<SchedulerTriggerModel> root = query.from(clazz);
		
		Predicate predicate = null;
		Join<Object, Object> joinTask = null;
		if (ValueUtils.hasValue(paramMap.get("taskCode"))) {
			joinTask = root.join("schedulerTask");
			joinTask.alias("schedulerTask");
			Predicate taskLike = builder.like(builder.upper(joinTask.get("code")),  "%" + paramMap.get("taskCode") +  "%");
			predicate = taskLike;
		}
		
		if (ValueUtils.hasValue(paramMap.get("taskName"))) {
			if(joinTask == null) {
				joinTask = root.join("schedulerTask");
				joinTask.alias("schedulerTask");
			}
			Predicate taskLike = builder.like(builder.upper(joinTask.get("name")),  "%" + ((String)paramMap.get("taskName")).toUpperCase() +  "%");
			if(predicate!=null) {
				predicate = builder.and(predicate, taskLike);
			} else
				predicate = taskLike;
		}

		if(joinTask == null) {
			root.fetch("schedulerTask");
		}
		
		Predicate inactiveFlag = builder.equal(builder.upper(root.get("inactiveFlag")), ApplicationConstants.NO);
		if(predicate!=null)
			predicate = builder.and(predicate, inactiveFlag);
		else
			predicate = inactiveFlag; 
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}