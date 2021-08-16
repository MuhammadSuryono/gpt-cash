package com.gpt.product.gpcash.corporate.logging.activity.repository;

import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.logging.activity.model.CorporateActivityLogModel;

@Repository
public interface CorporateActivityLogRepository extends JpaRepository<CorporateActivityLogModel, String>, CashRepository<CorporateActivityLogModel>{

	/**
	 * @param paramMap
	 * @param pageInfo
	 * @return Tuple "act" which is instance of {@link CorporateActivityLogModel} and "name" which is the userId of the field actionBy 
	 * @throws Exception
	 */
	Page<Tuple> searchActivityLog(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

}
