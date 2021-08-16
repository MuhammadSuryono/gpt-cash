package com.gpt.product.gpcash.corporate.pendingtaskadmin.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

public class CorporateAdminPendingTaskRepositoryImpl extends CashRepositoryImpl<CorporateAdminPendingTaskModel>
		implements CorporateAdminPendingTaskCustomRepository {
	
	@PersistenceContext
	private EntityManager em;

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "referenceNo"));
	}

	@Override
	public Page<CorporateAdminPendingTaskModel> searchPendingTask(CorporateAdminPendingTaskVO vo, Pageable pageInfo)
			throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateAdminPendingTaskModel> query = builder.createQuery(CorporateAdminPendingTaskModel.class);

		Root<CorporateAdminPendingTaskModel> root = query.from(CorporateAdminPendingTaskModel.class);

		Predicate predicate = null;
		if (ValueUtils.hasValue(vo.getReferenceNo())) {
			predicate = builder.like(builder.upper(root.get("referenceNo")),
					"%" + vo.getReferenceNo().toUpperCase() + "%");
		}

		if (ValueUtils.hasValue(vo.getCreatedBy())) {
			Predicate createdByEquals = builder.equal(builder.upper(root.get("createdBy")),
					vo.getCreatedBy().toUpperCase());
			if (predicate == null)
				predicate = createdByEquals;
			else
				predicate = builder.and(predicate, createdByEquals);
		}

		if (ValueUtils.hasValue(vo.getMenuCode())) {
			Join<Object, Object> joinMenu = root.join("menu");
			joinMenu.alias("menu");
			Predicate menuCodeEquals = builder.equal(builder.upper(joinMenu.get("code")), vo.getMenuCode().toUpperCase());
			
			if (predicate == null)
				predicate = menuCodeEquals;
			else
				predicate = builder.and(predicate, menuCodeEquals);
		}

		if (ValueUtils.hasValue(vo.getStatus())) {
			Predicate statusEquals = builder.equal(builder.upper(root.get("status")), vo.getStatus().toUpperCase());
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
