package com.gpt.product.gpcash.retail.logging.activity.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.logging.activity.model.CustomerActivityLogModel;

public class CustomerActivityLogRepositoryImpl extends CashRepositoryImpl<CustomerActivityLogModel>{
	
	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.DESC, "activityDate"));
	}
	
	public Page<Tuple> searchActivityLog(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);

		Root<CustomerActivityLogModel> root = query.from(CustomerActivityLogModel.class);
		Root<IDMUserModel> rootUser = query.from(IDMUserModel.class);
		
		Predicate predicate = builder.equal(root.get("actionBy"), rootUser.get("code"));
		
		Date fromDate = (Date)paramMap.get("fromDateVal");
		Date toDate = (Date)paramMap.get("toDateVal");
		
		if (ValueUtils.hasValue(fromDate) && ValueUtils.hasValue(toDate)) {
			Calendar calFrom = DateUtils.getEarliestDate(fromDate);
			Calendar calTo = DateUtils.getNextEarliestDate(toDate);
			
			predicate = builder.and(predicate,
							builder.greaterThanOrEqualTo(root.get("activityDate"), calFrom.getTime()),
							builder.lessThan(root.get("activityDate"), calTo.getTime())
						);
		}

		Object actionBy = paramMap.get("actionBy");
		if (ValueUtils.hasValue(actionBy)) {
			Predicate actionByEquals = builder.equal(root.get("actionBy"), actionBy);
			predicate = builder.and(predicate, actionByEquals);
		}
		
		String referenceNo = (String)paramMap.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			predicate = builder.and(predicate, builder.like(root.get("referenceNo"), "%" + referenceNo + "%"));
		}
		
		String isError = (String) paramMap.get("isError");
		if (ValueUtils.hasValue(isError)) {
			Predicate isErrorEquals = builder.equal(root.get("isError"), isError);
			predicate = builder.and(predicate, isErrorEquals);
		}

		Object activityLogMenuCode = paramMap.get("activityLogMenuCode");
		Object activityLogMenuList = paramMap.get("activityLogMenuList");
		if (ValueUtils.hasValue(activityLogMenuCode)) {
			Predicate menuCodeEquals = builder.equal(builder.upper(root.get("menuCode")), activityLogMenuCode);
			predicate = builder.and(predicate, menuCodeEquals);
		} else if (activityLogMenuList != null) {
			List<String> menuList = (ArrayList<String>) activityLogMenuList;
			Expression<String> exp = root.get("menuCode");
			predicate = builder.and(predicate, exp.in(menuList));
		}

		Object actionType = paramMap.get("actionType");
		if (ValueUtils.hasValue(actionType)) {
			Predicate statusEquals = builder.equal(builder.upper(root.get("actionType")), actionType);
			predicate = builder.and(predicate, statusEquals);
		}
		
		// paths for sorting with alias
		Map<String, Path<Object>> paths = new HashMap<>(1, 1);
		paths.put("activityDate", root.get("activityDate"));
		
		// select both activity and user's name
		query.multiselect(root.alias("act"), rootUser.get("name").alias("name"));
		
		if (predicate != null)
			query.where(predicate);

		Map<String, Root<?>> roots = new HashMap<>(2, 1);
		roots.put(root.getAlias(), root);
		roots.put(rootUser.getAlias(), rootUser);

		return createPageResult(pageInfo, builder, roots, paths, query, predicate);
	}
}
