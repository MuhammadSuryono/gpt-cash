package com.gpt.product.gpcash.corporate.workflow.repository;

import java.math.BigDecimal;
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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.CorporateWFEngine.Type;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;

public class CorporateTaskInstanceRepositoryImpl extends BasicRepositoryImpl<CorporateTaskInstance> implements CorporateTaskInstanceCustomRepository {

	@Override
	public Page<Tuple> findActiveTasksByUser(Map<String, Object> map, CorporateWFEngine.Type type, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		
		Root<CorporateProcessInstance> pi = query.from(CorporateProcessInstance.class);
		pi.alias("pi");
		
		Root<CorporateTaskInstance> ti = query.from(CorporateTaskInstance.class);
		ti.alias("ti");
		
		Root<?> pt;
		
		switch(type) {
			case CorporateAdmin:
				pt = query.from(CorporateAdminPendingTaskModel.class);
				break;
			default:
				pt = query.from(CorporateUserPendingTaskModel.class);
		}
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

		String loginId = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
		
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

		String corpAccount = (String)map.get("corpAccount");
		if(ValueUtils.hasValue(corpAccount)) {
			predicate = builder.and(predicate, builder.equal(pt.get("sourceAccount"), corpAccount));
		}
		
		BigDecimal trxAmountFrom = (BigDecimal)map.get("trxAmountFrom");
		BigDecimal trxAmountTo = (BigDecimal)map.get("trxAmountTo");
		if(trxAmountFrom != null || trxAmountTo != null) {
			// can only be IDR
			predicate = builder.and(predicate, builder.equal(pt.get("transactionCurrency"), "IDR"));
			if(trxAmountFrom != null) {
				predicate = builder.and(predicate, builder.greaterThanOrEqualTo(pt.get("transactionAmount"), trxAmountFrom));
			}
			if(trxAmountTo != null) {
				predicate = builder.and(predicate, builder.lessThanOrEqualTo(pt.get("transactionAmount"), trxAmountTo));
			}
		}
		
		String menuCode = (String)map.get("pendingTaskMenuCode");
		if (ValueUtils.hasValue(menuCode)) {
			predicate = builder.and(predicate, builder.equal(idm.get("code"), menuCode));
		}
		
		// paths for sorting with alias
		Map<String, Path<Object>> paths = new HashMap<>(20, 1);
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

		if(type == Type.CorporateUser) {
			paths.put("debitAccount", pt.get("sourceAccount"));
			paths.put("debitAccountName", pt.get("sourceAccountName"));
			paths.put("debitAccountCurrency", pt.get("sourceAccountCurrencyCode"));
	
			paths.put("transactionAmount", pt.get("transactionAmount"));
			paths.put("transactionCurrency", pt.get("transactionCurrency"));
	
			paths.put("creditAccount", pt.get("benAccount"));
			paths.put("creditAccountName", pt.get("benAccountName"));
			paths.put("creditAccountCurrency", pt.get("benAccountCurrencyCode"));
			// request add by BJB
			paths.put("instructionMode", pt.get("instructionMode"));
			paths.put("billingId", pt.get("billingId"));
			
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
				paths.get("actionDate").alias("actionDate"),
				paths.get("referenceNo").alias("referenceNo"),
				paths.get("debitAccount").alias("debitAccount"),
				paths.get("debitAccountName").alias("debitAccountName"),
				paths.get("debitAccountCurrency").alias("debitAccountCurrency"),
				paths.get("transactionAmount").alias("transactionAmount"),
				paths.get("transactionCurrency").alias("transactionCurrency"),
				paths.get("creditAccount").alias("creditAccount"),
				paths.get("creditAccountName").alias("creditAccountName"),
				paths.get("creditAccountCurrency").alias("creditAccountCurrency"),
				paths.get("uniqueKeyDisplay").alias("uniqueKeyDisplay"),
				paths.get("action").alias("action"),
				// request add by BJB
				paths.get("instructionMode").alias("instructionMode"),
				paths.get("billingId").alias("billingId"),
				// from idm user
				paths.get("actionBy").alias("actionBy"),
				paths.get("actionByName").alias("actionByName"),
				// from idm menu
				idm.get("code").alias("pendingTaskMenuCode"),
				paths.get("pendingTaskMenuName").alias("pendingTaskMenuName")
			);
			
		} else {
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
		}
		
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
	
	@Override
	public Page<CorporateUserPendingTaskModel> findPendingTasks(Map<String, Object> map, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateUserPendingTaskModel> query = builder.createQuery(CorporateUserPendingTaskModel.class);

		Root<CorporateUserPendingTaskModel> pt = query.from(CorporateUserPendingTaskModel.class);
		pt.alias("pt");
		
		Root<CorporateTaskInstance> ti = query.from(CorporateTaskInstance.class);
		ti.alias("ti");

		Predicate predicate = builder.equal(pt.get("id"), ti.get("processInstance").get("id"));
		
		String referenceNo = (String)map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			predicate = builder.and(predicate, builder.like(pt.get("referenceNo"), "%" + referenceNo + "%"));
		}
		
		String senderRefNo = (String)map.get("senderRefNo");
		if (ValueUtils.hasValue(senderRefNo)) {
			predicate = builder.and(predicate, builder.like(pt.get("senderRefNo"), "%" + senderRefNo + "%"));
		}
		
		String benRefNo = (String)map.get("benRefNo");
		if (ValueUtils.hasValue(benRefNo)) {
			predicate = builder.and(predicate, builder.like(pt.get("benRefNo"), "%" + benRefNo + "%"));
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

		String corpAccount = (String)map.get("corpAccount");
		if(ValueUtils.hasValue(corpAccount)) {
			predicate = builder.and(predicate, builder.equal(pt.get("sourceAccount"), corpAccount));
		}
		
		String corpId = (String)map.get(ApplicationConstants.CORP_ID);
		Path<Object> pathCorporateId = pt.get("corporate").get("id");
		if(ValueUtils.hasValue(corpId)) {
			predicate = builder.and(predicate, builder.equal(pathCorporateId, corpId));
		}
		
		Path<Object> pathMenuCode = pt.get("menu").get("code");
		
		String menuCode = (String)map.get("pendingTaskMenuCode");
		if (ValueUtils.hasValue(menuCode)) {
			predicate = builder.and(predicate, builder.equal(pathMenuCode, menuCode));
		}
		
		String status = (String)map.get("status");
		if (ValueUtils.hasValue(status)) {
			predicate = builder.and(predicate, builder.equal(pt.get("trxStatus"), TransactionStatus.valueOf(status)));
		}
		
		String errorTimeout = (String)map.get("errorTimeoutFlag");
		if (ValueUtils.hasValue(errorTimeout)) {
			predicate = builder.and(predicate, builder.equal(pt.get("errorTimeoutFlag"), errorTimeout));
		}
		
		String makerUserCode = (String)map.get("makerUserCode");
		if (ValueUtils.hasValue(makerUserCode)) {
			predicate = builder.and(predicate, builder.equal(pt.get("createdBy"), makerUserCode));
		}

		String userGroupId = (String) map.get("userGroupId");
		if (ValueUtils.hasValue(userGroupId)) {
			Predicate makers = builder.equal(pt.get("userGroupId"), userGroupId);

			Predicate approvers = builder.equal(ti.get("userGroupId"), userGroupId);
			approvers = builder.and(approvers, builder.isFalse(ti.get("canceled")));
			approvers = builder.and(approvers, builder.isNotNull(ti.get("endDate")));
			
			predicate = builder.and(predicate, builder.or(makers, approvers));
		}
		
		
		Map<String, Path<Object>> paths = new HashMap<>(6,1);
		paths.put("referenceNo", pt.get("referenceNo"));
		paths.put("activityDate", pt.get("activityDate"));
		paths.put("pendingTaskMenuCode", pathMenuCode);
		paths.put("sourceAccount", pt.get("sourceAccount"));
		paths.put("transactionAmount", pt.get("transactionAmount"));
		paths.put("status", pt.get("status"));
		
		query.select(pt).distinct(true);
		
		if (predicate != null)
			query.where(predicate);
		
		Map<String, Root<?>> roots = new HashMap<>(2, 1);
		roots.put(pt.getAlias(), pt);
		roots.put(ti.getAlias(), ti);

		return createPageResult(pageInfo, builder, roots, paths, query, predicate);
	}
	
	@Override
	public Page<CorporateAdminPendingTaskModel> findNonFinancialPendingTasks(Map<String, Object> map, Pageable pageInfo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateAdminPendingTaskModel> query = builder.createQuery(CorporateAdminPendingTaskModel.class);

		Root<CorporateAdminPendingTaskModel> pt = query.from(CorporateAdminPendingTaskModel.class);
		pt.alias("pt");
		
		Root<CorporateTaskInstance> ti = query.from(CorporateTaskInstance.class);
		ti.alias("ti");

		Predicate predicate = builder.equal(pt.get("id"), ti.get("processInstance").get("id"));
		
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
		
		String corpId = (String)map.get(ApplicationConstants.CORP_ID);
		Path<Object> pathCorporateId = pt.get("corporate").get("id");
		if(ValueUtils.hasValue(corpId)) {
			predicate = builder.and(predicate, builder.equal(pathCorporateId, corpId));
		}
		
		Path<Object> pathMenuCode = pt.get("menu").get("code");
		
		String menuCode = (String)map.get("pendingTaskMenuCode");
		if (ValueUtils.hasValue(menuCode)) {
			predicate = builder.and(predicate, builder.equal(pathMenuCode, menuCode));
		}
		
		String status = (String)map.get("status");
		if (ValueUtils.hasValue(status)) {
			predicate = builder.and(predicate, builder.equal(pt.get("status"), status));
		}

		Map<String, Path<Object>> paths = new HashMap<>(6,1);
		paths.put("referenceNo", pt.get("referenceNo"));
		paths.put("activityDate", pt.get("createdDate"));
		paths.put("pendingTaskMenuCode", pathMenuCode);
		paths.put("status", pt.get("status"));
		
		query.select(pt).distinct(true);
		
		if (predicate != null)
			query.where(predicate);
		
		Map<String, Root<?>> roots = new HashMap<>(2, 1);
		roots.put(pt.getAlias(), pt);
		roots.put(ti.getAlias(), ti);

		return createPageResult(pageInfo, builder, roots, paths, query, predicate);
	}
}
