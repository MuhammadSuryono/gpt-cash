package com.gpt.product.gpcash.workflow.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.common.repository.BasicRepositoryImpl;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.pendingtask.model.PendingTaskModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.workflow.WFEngine;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;
import com.gpt.product.gpcash.workflow.model.TaskInstance;

public class TaskInstanceRepositoryImpl extends BasicRepositoryImpl<TaskInstance> implements TaskInstanceCustomRepository {

	@Override
	public Page<Tuple> findActiveTasksByUser(Map<String, Object> map, WFEngine.Type type, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		
		Root<ProcessInstance> pi = query.from(ProcessInstance.class);
		pi.alias("pi");
		
		Root<TaskInstance> ti = query.from(TaskInstance.class);
		ti.alias("ti");
		
		Root<?> pt;
		
		pt = query.from(PendingTaskModel.class);
		pt.alias("pt");
		
		Root<IDMMenuModel> idm = query.from(IDMMenuModel.class);
		idm.alias("idm");

		Root<IDMUserModel> user = query.from(IDMUserModel.class);
		idm.alias("user");

		Predicate predicate = builder.equal(pi.get("id"), ti.get("processInstance"));
		predicate = builder.and(predicate, builder.equal(pi.get("id"), pt.get("id")));
		predicate = builder.and(predicate, builder.isNull(pi.get("endDate")));
		predicate = builder.and(predicate, builder.isFalse(ti.get("canceled")));
		predicate = builder.and(predicate, builder.isNull(ti.get("endDate")));
		predicate = builder.and(predicate, builder.equal(ti.get("user"), user.get("code")));
		predicate = builder.and(predicate, builder.equal(pt.get("menu").get("code"), idm.get("code")));

		String loginId = "";
		loginId = (String)map.get(ApplicationConstants.LOGIN_USERID);
		
		predicate = builder.and(predicate, builder.equal(ti.get("user"), loginId));
		
		String referenceNo = (String)map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			predicate = builder.and(predicate, builder.like(pt.get("referenceNo"), "%" + referenceNo + "%"));
		}

		Date creationDateFrom = (Date)map.get("creationDateFrom");
		if(creationDateFrom != null) {
			creationDateFrom = DateUtils.getEarliestDate(creationDateFrom).getTime();
			predicate = builder.and(predicate, builder.greaterThanOrEqualTo(pt.get("createdDate"), creationDateFrom));
		}
		
		Date creationDateTo = (Date)map.get("creationDateTo");
		if(creationDateTo != null) {
			creationDateTo = DateUtils.getNextEarliestDate(creationDateTo).getTime();
			predicate = builder.and(predicate, builder.lessThan(pt.get("createdDate"), creationDateTo));
		}
		
		Date instructionDateFrom = (Date)map.get("instructionDateFrom");
		if(instructionDateFrom != null) {
			instructionDateFrom = DateUtils.getEarliestDate(instructionDateFrom).getTime();
			predicate = builder.and(predicate, builder.greaterThanOrEqualTo(pt.get("instructionDate"), instructionDateFrom));
		}
		
		Date instructionDateTo = (Date)map.get("instructionDateTo");
		if(instructionDateTo != null) {
			instructionDateTo = DateUtils.getNextEarliestDate(instructionDateTo).getTime();
			predicate = builder.and(predicate, builder.lessThan(pt.get("instructionDate"), instructionDateTo));
		}

		String menuCode = (String)map.get("pendingTaskMenuCode");
		if (ValueUtils.hasValue(menuCode)) {
			predicate = builder.and(predicate, builder.equal(idm.get("code"), menuCode));
		}
		
		// paths for sorting with alias
		Map<String, Path<Object>> paths = new HashMap<>(18, 1);
		// from process instance
		paths.put("currApprLvCount", pi.get("currApprLvCount"));
		paths.put("currApprLv", pi.get("currApprLv"));
		// from task instance
		paths.put("startDate", ti.get("startDate"));
		// from pending task
		paths.put("referenceNo", pt.get("referenceNo"));
		paths.put("actionBy", user.get("userId"));
		paths.put("actionByName", user.get("name"));
		paths.put("actionDate", pt.get("createdDate"));
		paths.put("uniqueKeyDisplay", pt.get("uniqueKeyDisplay"));
		paths.put("action", pt.get("action"));
		paths.put("pendingTaskMenuName", idm.get("name"));

		query.multiselect(
			// from process instance
			pi.get("id").alias("pendingTaskId"),
			paths.get("currApprLvCount").alias("currApprLvCount"),
			paths.get("currApprLv").alias("currApprLv"),
			pi.joinList("approvalMatrix").alias("approvalMatrix"),
			// from task instance
			ti.get("id").alias("taskId"),
			ti.get("stageId").alias("stageId"),
			paths.get("startDate").alias("startDate"),
			// from pending task
			paths.get("referenceNo").alias("referenceNo"),
			paths.get("actionDate").alias("actionDate"),
			paths.get("uniqueKeyDisplay").alias("uniqueKeyDisplay"),
			paths.get("action").alias("action"),
			// from idm user
			paths.get("actionBy").alias("actionBy"),
			paths.get("actionByName").alias("actionByName"),
			// from idm menu
			idm.get("code").alias("pendingTaskMenuCode"),
			paths.get("pendingTaskMenuName").alias("pendingTaskMenuName")
		);

		if(predicate!=null)
			query.where(predicate);
		
		Map<String, Root<?>> roots = new HashMap<>(5, 1);
		roots.put(ti.getAlias(), ti);
		roots.put(pi.getAlias(), pi);
		roots.put(pt.getAlias(), pt);
		roots.put(user.getAlias(), user);
		roots.put(idm.getAlias(), idm);
		
		return createPageResult(pageInfo, builder, roots, paths, query, predicate);
	}	
}
