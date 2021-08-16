package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInternationalModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;

@AutoDiscoveryImpl
public interface CustomerBeneficiaryListInternationalService extends CustomerUserWorkflowService {
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;

	CustomerBeneficiaryListInternationalModel getExistingRecord(String accountNo, String customerId, boolean isThrowError)
			throws Exception;
	
	Map<String, Object> searchCustomerBeneficiary(String customerId)
			throws ApplicationException, BusinessException;
	
	CustomerBeneficiaryListInternationalModel findCustomerBeneficiary(String benId) throws Exception;
	
	void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode,String isBenIdentity,
			String isBenAffiliated, String benCountry,String bankCode, String createdBy) throws Exception;

}