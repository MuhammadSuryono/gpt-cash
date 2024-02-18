package com.gpt.product.gpcash.corporate.transaction.domestic.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.query.Query;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.repository.CashRepositoryImpl;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transaction.domestic.model.DomesticTransferModel;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;

public class DomesticTransferRepositoryImpl extends CashRepositoryImpl<DomesticTransferModel> implements DomesticTransferCustomRepository{
	@PersistenceContext
	protected EntityManager em;
	
	@Override
	public List<CorporateUserPendingTaskModel> findDomesticReport(Map<String, Object> map) {		
		Predicate predicate = null;
		
		List<CorporateUserPendingTaskModel> resultList = new ArrayList<>();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		
		CriteriaQuery<CorporateUserPendingTaskModel> query = builder.createQuery(CorporateUserPendingTaskModel.class);
		
		Root<CorporateUserPendingTaskModel> pt = query.from(CorporateUserPendingTaskModel.class);
		pt.alias("pt");
		
		String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
		if (ValueUtils.hasValue(referenceNo)) {
			predicate = builder.like(pt.get("referenceNo"), referenceNo);
		} else {
			String serviceCd = (String) map.get("service");
			String domMenu = "MNU_GPCASH_F_FUND_DOMESTIC";
			String sipkdMenu = "MNU_GPCASH_F_SIPKD";
			String menuCode = ApplicationConstants.EMPTY_STRING;
			
			if (serviceCd != null && !ApplicationConstants.EMPTY_STRING.equals(serviceCd)) {
				if (serviceCd.indexOf("SIPKD") > -1) {
					menuCode = sipkdMenu;
				} else {
					menuCode = domMenu;
				}
			}
			
			// 0 Created Date
			// 1 Instruction Date
			String dataType = (String) map.get("dateType");
			String createOrInstruction = "createdDate";
			if ("1".equals(dataType)) {
				createOrInstruction = "instructionDate";
			}
			
			Path<Object> pathMenuCode = pt.get("menu").get("code");
			
			if(menuCode.equals(ApplicationConstants.EMPTY_STRING)) {
				predicate = builder.equal(pathMenuCode, domMenu);
				predicate = builder.or(predicate, builder.equal(pathMenuCode, sipkdMenu));
			} else {
				predicate = builder.equal(pathMenuCode, menuCode);
			}
			
			String corpId = (String) map.get(ApplicationConstants.CORP_ID);
			Path<Object> pathCorporateId = pt.get("corporate").get("id");
			if(ValueUtils.hasValue(corpId)) {
				predicate = builder.and(predicate, builder.equal(pathCorporateId, corpId));
			}
			
			Path<Object> pathServiceCd = pt.get("transactionService").get("code");
			if(ValueUtils.hasValue(serviceCd)) {
				predicate = builder.and(predicate, builder.equal(pathServiceCd, serviceCd));
			} else {
				if (sipkdMenu.equals(menuCode)){
					predicate = builder.and(predicate, pathServiceCd.in(Arrays.asList("GPT_FTR_SIPKD_DOM_LLG","GPT_FTR_SIPKD_DOM_RTGS","GPT_FTR_SIPKD_ONLINE")));
				}
			}
			
			String corpAccount = (String)map.get("corpAccount");
			if(ValueUtils.hasValue(corpAccount)) {
				predicate = builder.and(predicate, builder.equal(pt.get("sourceAccount"), corpAccount));
			}
			
			Date creationDateFrom = (Date)map.get("creationDateFrom");
			if(creationDateFrom != null) {
				creationDateFrom = DateUtils.getEarliestDate(creationDateFrom).getTime();
				predicate = builder.and(predicate, builder.greaterThanOrEqualTo(pt.get(createOrInstruction), creationDateFrom));
			}
			Date creationDateTo = (Date)map.get("creationDateTo");
			if(creationDateTo != null) {
				creationDateTo = DateUtils.getNextEarliestDate(creationDateTo).getTime();
				predicate = builder.and(predicate, builder.lessThan(pt.get(createOrInstruction), creationDateTo));
			}
			
			String status = (String)map.get("status");
			if (ValueUtils.hasValue(status)) {
				predicate = builder.and(predicate, builder.equal(pt.get("trxStatus"), TransactionStatus.valueOf(status)));
			}
		}
		
		query.select(pt)
			.where(predicate).orderBy(builder.asc(pt.get("createdDate")));
		
		Query<CorporateUserPendingTaskModel> queryExecute = (Query<CorporateUserPendingTaskModel>) em.createQuery(query);
		logger.debug("Query findDomesticReport String : "+ queryExecute.getQueryString());
		resultList.addAll(queryExecute.getResultList());
		
		return resultList;
	}
}
