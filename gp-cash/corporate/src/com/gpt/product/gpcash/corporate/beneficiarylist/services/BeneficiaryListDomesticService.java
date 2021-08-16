package com.gpt.product.gpcash.corporate.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface BeneficiaryListDomesticService extends CorporateUserWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	void saveBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;
	
	void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode,
			String bankCode, String createdBy, boolean isBenOnline) throws Exception;

	BeneficiaryListDomesticModel findBeneficiary(String benId) throws Exception;

	BeneficiaryListDomesticModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception;

	Map<String, Object> searchBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException;
}