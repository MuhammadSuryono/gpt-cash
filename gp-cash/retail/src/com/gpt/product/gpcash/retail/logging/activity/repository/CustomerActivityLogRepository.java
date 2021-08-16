package com.gpt.product.gpcash.retail.logging.activity.repository;

import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.logging.activity.model.CustomerActivityLogModel;

@Repository
public interface CustomerActivityLogRepository extends JpaRepository<CustomerActivityLogModel, String>, CashRepository<CustomerActivityLogModel>{

	/**
	 * @param paramMap
	 * @param pageInfo
	 * @return Tuple "act" which is instance of {@link CustomerActivityLogModel} and "name" which is the userId of the field actionBy 
	 * @throws Exception
	 */
	Page<Tuple> searchActivityLog(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;

}
