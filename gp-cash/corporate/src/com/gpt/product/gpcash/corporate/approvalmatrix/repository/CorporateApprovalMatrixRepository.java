package com.gpt.product.gpcash.corporate.approvalmatrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixDetailModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixMasterModel;

@Repository
public interface CorporateApprovalMatrixRepository extends JpaRepository<CorporateApprovalMatrixMasterModel, String>, CashRepository<CorporateApprovalMatrixMasterModel>{
	
	CorporateApprovalMatrixMasterModel findByCorporateIdAndMenuCodeAndCurrencyCode(String corporateId, String menuCode, String currencyCode) throws Exception;
	
	List<CorporateApprovalMatrixMasterModel> findByCorporateId(String corporateId) throws Exception;
	
	CorporateApprovalMatrixMasterModel findByCorporateIdAndMenuCode(String corporateId, String menuCode) throws Exception;

	@Query("select d from CorporateApprovalMatrixDetailModel d "
			+ "where d.corporateApprovalMatrixSub.corporateApprovalMatrixMaster.menu.code = ?1 "
			+ "and d.corporateApprovalMatrixSub.corporateApprovalMatrixMaster.corporate.id = ?2 "
			+ "order by d.corporateApprovalMatrixSub.longAmountLimit, d.sequenceNo")	
	List<CorporateApprovalMatrixDetailModel> findDetailByMenuCodeAndCorporateId(String menuCode, String corporateId);

}
