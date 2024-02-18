package com.gpt.product.gpcash.corporate.transactionholidayupdate.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.common.repository.BasicRepository;
import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.model.HolidayTransactionModel;

@Repository
public interface HolidayTransactionCustomRepository extends BasicRepository<HolidayTransactionModel> {

	Page<HolidayTransactionModel> findHolidayTransactions(Map<String, Object> map, Pageable pageInfo);
	
}
