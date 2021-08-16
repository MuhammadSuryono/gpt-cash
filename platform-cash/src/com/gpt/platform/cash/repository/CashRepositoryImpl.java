package com.gpt.platform.cash.repository;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.gpt.component.common.repository.BasicRepositoryImpl;
import com.gpt.component.common.repository.SortOnlyPageRequest;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;

public class CashRepositoryImpl<T> extends BasicRepositoryImpl<T> implements CashRepository<T> {
	protected final Sort defaultSort;
	
	public CashRepositoryImpl() {
		defaultSort = getDefaultSort();
	}
	
	/**
	 * This default sort only affects the result when you use method {@link #createPageResult(Pageable, CriteriaBuilder, Root, CriteriaQuery, Predicate)}
	 * or {@link #createPageResult(Pageable, CriteriaBuilder, Map, Map, CriteriaQuery, Predicate)}
	 * @return
	 */
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.ASC, "name"));
	}
	
	@Override
	public Page<T> search(Map<String, Object> paramMap, Pageable pageInfo) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("paramMap : " + paramMap);

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(clazz);
		
		Root<T> root = query.from(clazz);
		
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

		Predicate isDeleted = builder.equal(builder.upper(root.get("deleteFlag")), ApplicationConstants.NO);
		if(predicate!=null)
			predicate = builder.and(predicate, isDeleted);
		else
			predicate = isDeleted; 
		
		if(predicate!=null)
			query.where(predicate);
		
		return createPageResult(pageInfo, builder, root, query, predicate);
	}
	
	protected Pageable adjustPageInfoForDefaultSorting(Pageable pageInfo) {
		if(pageInfo != null) {
			if(pageInfo.getSort() == null && defaultSort != null) {
				pageInfo = new PageRequest(pageInfo.getPageNumber(), pageInfo.getPageSize(), defaultSort);
			}
		} else if(defaultSort != null){
			pageInfo = new SortOnlyPageRequest(defaultSort);
		}
		return pageInfo;
	}
	
	protected <R> Page<R> createPageResult(Pageable pageInfo, CriteriaBuilder builder, Root<?> root, CriteriaQuery<R> query, Predicate predicate) {
		return super.createPageResult(adjustPageInfoForDefaultSorting(pageInfo), builder, root, query, predicate);
	}

	@Override
	protected <R> Page<R> createPageResult(Pageable pageInfo, CriteriaBuilder builder, Map<String, Root<?>> roots, Map<String, Path<Object>> paths, CriteriaQuery<R> query, Predicate predicate) {
		return super.createPageResult(adjustPageInfoForDefaultSorting(pageInfo), builder, roots, paths, query, predicate);
	}
	
}
