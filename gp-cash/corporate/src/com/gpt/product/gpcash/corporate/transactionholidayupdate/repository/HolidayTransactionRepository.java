package com.gpt.product.gpcash.corporate.transactionholidayupdate.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.transactionholidayupdate.model.HolidayTransactionModel;

@Repository
public interface HolidayTransactionRepository extends JpaRepository<HolidayTransactionModel, String>, HolidayTransactionCustomRepository{

	@Query("from HolidayTransactionModel holidayTrx "
			+ "where holidayTrx.referenceNo like ?1 and holidayTrx.isProcessed = 'N' "
			+ "order by holidayTrx.instructionDate DESC")
	List<HolidayTransactionModel> findByReferenceNo(String referenceNo) throws Exception;
	
	
}
