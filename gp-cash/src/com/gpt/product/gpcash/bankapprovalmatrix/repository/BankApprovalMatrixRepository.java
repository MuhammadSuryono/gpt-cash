package com.gpt.product.gpcash.bankapprovalmatrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixDetailModel;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixModel;

@Repository
public interface BankApprovalMatrixRepository extends JpaRepository<BankApprovalMatrixModel, String>, CashRepository<BankApprovalMatrixModel> {

	@Modifying
	@Query("delete from BankApprovalMatrixDetailModel d where d.bankApprovalMatrix.id = ?1")
	void deleteDetailByBankApprovalMatrixId(String bankApprovalMatrixId);
	
	@Query("from BankApprovalMatrixModel m "
			+ "where deleteFlag = 'N' "
			+ "order by m.menu.name")
	List<BankApprovalMatrixModel> findAll();
	
	@Query("select d from BankApprovalMatrixModel m "
			+ "join m.bankApprovalMatrixDetail d "
			+ "join fetch d.approvalLevel "
			+ "where m.menu.code = ?1 "
			+ "order by d.sequenceNo")
	List<BankApprovalMatrixDetailModel> findByMenuCode(String menuCode);

}
