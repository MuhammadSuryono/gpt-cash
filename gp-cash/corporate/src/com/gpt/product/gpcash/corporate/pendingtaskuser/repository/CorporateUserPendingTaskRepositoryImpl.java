package com.gpt.product.gpcash.corporate.pendingtaskuser.repository;

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
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;

public class CorporateUserPendingTaskRepositoryImpl extends CashRepositoryImpl<CorporateUserPendingTaskModel>
		implements CorporateUserPendingTaskCustomRepository {

	@PersistenceContext
	private EntityManager em;

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "referenceNo"));
	}

	@Override
	public Page<CorporateUserPendingTaskModel> searchPendingTask(CorporateUserPendingTaskVO vo, Pageable pageInfo)
			throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CorporateUserPendingTaskModel> query = builder.createQuery(CorporateUserPendingTaskModel.class);

		Root<CorporateUserPendingTaskModel> root = query.from(CorporateUserPendingTaskModel.class);

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
			Predicate menuCodeEquals = builder.equal(builder.upper(joinMenu.get("code")),
					vo.getMenuCode().toUpperCase());
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

//	@Override
//	public List<CorporateUserPendingTaskModel> getByServiceCodeAndInstructionDate(String serviceCode,
//			Timestamp instructionDate) throws Exception {
//		CriteriaBuilder builder = em.getCriteriaBuilder();
//		CriteriaQuery<CorporateUserPendingTaskModel> query = builder.createQuery(CorporateUserPendingTaskModel.class);
//
//		Root<CorporateUserPendingTaskModel> root = query.from(CorporateUserPendingTaskModel.class);
//
//		Calendar calFrom = Calendar.getInstance();
//		calFrom.setTime(instructionDate);
//		calFrom.set(Calendar.HOUR_OF_DAY, 0);
//		calFrom.set(Calendar.MINUTE, 0);
//		calFrom.set(Calendar.SECOND, 0);
//		calFrom.set(Calendar.MILLISECOND, 0);
//
//		Calendar calTo = Calendar.getInstance();
//		calTo.setTime(instructionDate);
//		calTo.set(Calendar.HOUR_OF_DAY, 0);
//		calTo.set(Calendar.MINUTE, 0);
//		calTo.set(Calendar.SECOND, 0);
//		calTo.set(Calendar.MILLISECOND, 0);
//		calTo.add(Calendar.DAY_OF_MONTH, 1);
//
//		Predicate predicateInstructionDate = builder.and(builder.greaterThanOrEqualTo(root.get("instructionDate"), calFrom.getTime()),
//				builder.lessThan(root.get("instructionDate"), calTo.getTime()));
//		
//		Join<Object, Object> joinTransactionServiceCode = root.join("transactionService");
//		joinTransactionServiceCode.alias("transactionService");
//		Predicate predicateTransactionServiceCode = builder.equal(builder.upper(joinTransactionServiceCode.get("code")), serviceCode);
//		
//		query.where(builder.and(predicateInstructionDate, predicateTransactionServiceCode));
//
//		return em.createQuery(query).getResultList();
//	}

}