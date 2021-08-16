package com.gpt.product.gpcash.corporate.corporateaccountgroup.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;
import com.gpt.product.gpcash.account.model.AccountModel;

@AutoDiscoveryImpl
public interface CorporateAccountGroupService extends CorporateAdminWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String createdBy) throws ApplicationException, BusinessException;
	
	void updateCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String updatedBy) throws ApplicationException, BusinessException;
	
	List<Map<String, Object>> searchByCorporateId(String corporateId, boolean isGetDetail) throws ApplicationException, BusinessException;
	
	AccountModel searchAccountByAccountNoForInquiryOnly(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException;
	
	List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailByBranchForInquiryOnly(String corporateId, String userCode, String branchCode) throws ApplicationException, BusinessException;
	
	List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForInquiryOnly(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForDebitOnly(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupDetailForDebitOnlyGetMap(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForCreditOnly(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupDetailForCreditOnlyGetMap(String corporateId, String userCode) throws ApplicationException, BusinessException;

	int getCountCorporateIdAndGroupId(String corporateId, String corporateAccountGroupId)
			throws ApplicationException, BusinessException;

	void deleteCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String deletedBy,
			String corporateId) throws ApplicationException, BusinessException;

	List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(
			String corporateId, String userCode, List<String> accountTypeList)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccountGroupDetailForInquiryOnlyGetMap(String corporateId, String userCode,
			List<String> accountTypeList) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccountGroupDetailForInquiryOnlyGetMapWithProductName(String corporateId,
			String userCode, List<String> accountTypeList) throws ApplicationException, BusinessException;

	Map<String, Object> searchCorporateAccountGroupDetailForDebitOnlyGetMapForBank(String corporateId)
			throws ApplicationException, BusinessException;
	
	public CorporateAccountGroupDetailModel searchCorporateAccountByAccountNo(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException;
	
	public CorporateAccountGroupDetailModel searchCorporateAccountByAccountNoForCredit(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupDetailVirtualAccountForInquiryOnly(String corporateId, String userCode, List<String> accountTypeList, boolean isGetProductDescription) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCorporateAccountGroupDetailNonVirtualAccountForInquiryOnlyGetMap(String corporateId, String userCode, List<String> accountTypeList, boolean isGetProductDescription) throws ApplicationException, BusinessException;
}
