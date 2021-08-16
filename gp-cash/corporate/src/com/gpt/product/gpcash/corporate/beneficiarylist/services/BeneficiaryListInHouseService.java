package com.gpt.product.gpcash.corporate.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface BeneficiaryListInHouseService extends CorporateUserWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	void saveBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String createdBy, String isVirtualAccount) throws Exception;

	BeneficiaryListInHouseModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception;
	
	Map<String, Object> searchBeneficiaryGroup(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> inquiryVirtualAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
}
