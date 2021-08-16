package com.gpt.product.gpcash.retail.pendingtaskuser.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
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
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

public class CustomerUserPendingTaskRepositoryImpl extends CashRepositoryImpl<CustomerUserPendingTaskModel>
		implements CustomerUserPendingTaskCustomRepository {

	@PersistenceContext
	private EntityManager em;

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "referenceNo"));
	}

	@Override
	public Page<CustomerUserPendingTaskModel> searchPendingTask(Map<String, Object> map, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CustomerUserPendingTaskModel> query = builder.createQuery(CustomerUserPendingTaskModel.class);

		Root<CustomerUserPendingTaskModel> pt = query.from(CustomerUserPendingTaskModel.class);
		pt.alias("pt");

		String custId = (String) map.get(ApplicationConstants.CUST_ID);
		Path<Object> pathCustomerId = pt.get("customer").get("id");
		Predicate predicate = builder.equal(pathCustomerId, custId);

		String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			predicate = builder.and(predicate, builder.like(pt.get("referenceNo"), "%" + referenceNo + "%"));
		}

		Date creationDateFrom = (Date) map.get("creationDateFrom");
		if (creationDateFrom != null) {
			creationDateFrom = DateUtils.getEarliestDate(creationDateFrom).getTime();
			predicate = builder.and(predicate, builder.greaterThanOrEqualTo(pt.get("createdDate"), creationDateFrom));
		}

		Date creationDateTo = (Date) map.get("creationDateTo");
		if (creationDateTo != null) {
			creationDateTo = DateUtils.getNextEarliestDate(creationDateTo).getTime();
			predicate = builder.and(predicate, builder.lessThan(pt.get("createdDate"), creationDateTo));
		}

		Date instructionDateFrom = (Date) map.get("instructionDateFrom");
		if (instructionDateFrom != null) {
			instructionDateFrom = DateUtils.getEarliestDate(instructionDateFrom).getTime();
			predicate = builder.and(predicate,
					builder.greaterThanOrEqualTo(pt.get("instructionDate"), instructionDateFrom));
		}

		Date instructionDateTo = (Date) map.get("instructionDateTo");
		if (instructionDateTo != null) {
			instructionDateTo = DateUtils.getNextEarliestDate(instructionDateTo).getTime();
			predicate = builder.and(predicate, builder.lessThan(pt.get("instructionDate"), instructionDateTo));
		}

		String custAccount = (String) map.get("custAccount");
		if (ValueUtils.hasValue(custAccount)) {
			predicate = builder.and(predicate, builder.equal(pt.get("sourceAccount"), custAccount));
		}

		Path<Object> pathMenuCode = pt.get("menu").get("code");

		String menuCode = (String) map.get("pendingTaskMenuCode");
		if (ValueUtils.hasValue(menuCode)) {
			predicate = builder.and(predicate, builder.equal(pathMenuCode, menuCode));
		}

		String status = (String) map.get("status");
		if (ValueUtils.hasValue(status)) {
			predicate = builder.and(predicate,
					builder.equal(pt.get("trxStatus"), CustomerTransactionStatus.valueOf(status)));
		}

		Map<String, Path<Object>> paths = new HashMap<>(6, 1);
		paths.put("referenceNo", pt.get("referenceNo"));
		paths.put("activityDate", pt.get("activityDate"));
		paths.put("pendingTaskMenuCode", pathMenuCode);
		paths.put("sourceAccount", pt.get("sourceAccount"));
		paths.put("transactionAmount", pt.get("transactionAmount"));
		paths.put("status", pt.get("status"));

		query.select(pt).distinct(true);

		if (predicate != null)
			query.where(predicate);

		Map<String, Root<?>> roots = new HashMap<>(1, 1);
		roots.put(pt.getAlias(), pt);

		return createPageResult(pageInfo, builder, roots, paths, query, predicate);
	}
}