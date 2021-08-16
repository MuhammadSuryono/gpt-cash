package com.gpt.product.gpcash.corporate.transaction.pendingupload.repository;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;

public class PendingUploadRepositoryImpl extends CashRepositoryImpl<PendingUploadModel> implements PendingUploadCustomRepository {

	@Override
	protected Sort getDefaultSort() {
		return new Sort(new Order(Direction.DESC, "uploadDate"));
	}	
	
	@Override
	public Page<PendingUploadModel> searchPendingUpload(Map<String,Object> map, Pageable pageInfo) throws Exception {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PendingUploadModel> query = builder.createQuery(PendingUploadModel.class);

		Root<PendingUploadModel> root = query.from(PendingUploadModel.class);

		Predicate predicate = builder.equal(builder.upper(root.get("isProcessed")), "N");		
		predicate = builder.and(predicate, builder.equal(builder.upper(root.get("deleteFlag")), "N"));
		predicate = builder.and(predicate, builder.isNotNull(root.get("status")));
		
		String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
		if (ValueUtils.hasValue(userCode)) {
			Predicate createdByEquals = builder.equal(builder.upper(root.get("createdBy")), userCode.toUpperCase());
			if (predicate == null)
				predicate = createdByEquals;
			else
				predicate = builder.and(predicate, createdByEquals);
		}
		
		String menuCode = (String)map.get(ApplicationConstants.STR_MENUCODE);
		if (ValueUtils.hasValue(menuCode)) {
			Join<Object, Object> joinMenu = root.join("menu");
			joinMenu.alias("menu");
			Predicate menuCodeEquals = builder.equal(builder.upper(joinMenu.get("code")), menuCode.toUpperCase());
			if (predicate == null)
				predicate = menuCodeEquals;
			else
				predicate = builder.and(predicate, menuCodeEquals);
		}
				
		query.where(predicate);

		return createPageResult(pageInfo, builder, root, query, predicate);
	}

}
