package com.gpt.product.gpcash.corporate.transaction.payroll.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.payroll.model.PayrollDetailModel;
import com.gpt.product.gpcash.corporate.transaction.payroll.model.PayrollModel;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollModel, String>, CashRepository<PayrollModel> {
	
	@Query("select p from PayrollModel p "
			+ "join fetch p.corporate "
			+ "join fetch p.service "
			+ "join fetch p.sourceAccount acct "
			+ "join fetch acct.currency "
			+ "join fetch acct.accountType "
			+ "join fetch p.corporateUserGroup "
			+ "join fetch p.application "
			+ "where p.isProcessed = 'N' and p.isHostProcessed = 'N' and p.instructionMode = ?1 and p.instructionDate between ?2 and ?3")
	List<PayrollModel> findTransactionForSchedulerByInstructionMode(String instructionMode, Timestamp startInstructionDate, Timestamp endInstructionDate);

	PayrollModel findByIdAndIsHostProcessed(String id, String isHostProcessed);
	
	@Query("from PayrollDetailModel dtl "
			+ "where dtl.payroll.id = ?1 order by dtl.idx")	
	Page<PayrollDetailModel> searchPayrollDetailById(String id, Pageable pageInfo) throws Exception;
	
	PayrollModel findByIdAndIsProcessed(String id, String isProcessed);
	
	
}
