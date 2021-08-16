package com.gpt.component.logging.repository;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.logging.model.ActivityLogModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.platform.cash.utils.DateUtils;

public class ActivityLogRepositoryImpl extends CashRepositoryImpl<ActivityLogModel> {
	
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.DESC, "activityDate"));
	}
	
	public Page<ActivityLogModel> searchActivityLog(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ActivityLogModel> query = builder.createQuery(ActivityLogModel.class);

		Root<ActivityLogModel> root = query.from(ActivityLogModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(paramMap.get("fromDateVal")) && ValueUtils.hasValue(paramMap.get("toDateVal"))) {
			Date fromDate = (Date)paramMap.get("fromDateVal");
			Date toDate = (Date)paramMap.get("toDateVal");
			
			Calendar calFrom = DateUtils.getEarliestDate(fromDate);
			Calendar calTo = DateUtils.getNextEarliestDate(toDate);
			
			predicate = builder.and(
							builder.greaterThanOrEqualTo(root.get("activityDate"), calFrom.getTime()),
							builder.lessThan(root.get("activityDate"), calTo.getTime())
						);
		}
		
		String referenceNo = (String)paramMap.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			Predicate referenceNoLike = builder.like(root.get("referenceNo"), "%" + referenceNo + "%");
			if (predicate == null)
				predicate = referenceNoLike;
			else
				predicate = builder.and(predicate, referenceNoLike);
		}

		if (ValueUtils.hasValue(paramMap.get("actionBy"))) {
			Predicate createdByEquals = builder.equal(builder.upper(root.get("actionBy")),
					((String) paramMap.get("actionBy")).toUpperCase());
			if (predicate == null)
				predicate = createdByEquals;
			else
				predicate = builder.and(predicate, createdByEquals);
		}

		if (ValueUtils.hasValue(paramMap.get("activityLogMenuCode"))) {
			Predicate menuCodeEquals = builder.equal(builder.upper(root.get("menuCode")),
					((String) paramMap.get("activityLogMenuCode")).toUpperCase());
			if (predicate == null)
				predicate = menuCodeEquals;
			else
				predicate = builder.and(predicate, menuCodeEquals);
		}
		
		String isError = (String) paramMap.get("isError");
		if (ValueUtils.hasValue(isError)) {
			Predicate isErrorEquals = builder.equal(root.get("isError"), isError);
			if (predicate == null)
				predicate = isErrorEquals;
			else
				predicate = builder.and(predicate, isErrorEquals);
		}

		if (ValueUtils.hasValue(paramMap.get("actionType"))) {
			Predicate statusEquals = builder.equal(builder.upper(root.get("actionType")),
					((String) paramMap.get("actionType")).toUpperCase());
			if (predicate == null)
				predicate = statusEquals;
			else
				predicate = builder.and(predicate, statusEquals);
		}

		if (predicate != null)
			query.where(predicate);

		return createPageResult(pageInfo, builder, root, query, predicate);
	}
}
