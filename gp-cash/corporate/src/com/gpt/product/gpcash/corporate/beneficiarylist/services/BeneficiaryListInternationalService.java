package com.gpt.product.gpcash.corporate.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface BeneficiaryListInternationalService extends CorporateUserWorkflowService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;

	BeneficiaryListInternationalModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError)
			throws Exception;
	
	Map<String, Object> searchBeneficiary(String corporateId, String userCode)
			throws ApplicationException, BusinessException;
	
	BeneficiaryListInternationalModel findBeneficiary(String benId) throws Exception;
	
	void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode,String isBenIdentity,
			String isBenAffiliated, String benCountry,String bankCode, String createdBy) throws Exception;

}