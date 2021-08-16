package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;

@AutoDiscoveryImpl
public interface CustomerBeneficiaryListDomesticService extends CustomerUserWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	void saveCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;
	
	void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode,
			String bankCode, String createdBy) throws Exception;

	CustomerBeneficiaryListDomesticModel findCustomerBeneficiary(String benId) throws Exception;

	CustomerBeneficiaryListDomesticModel getExistingRecord(String accountNo, String customerId, boolean isThrowError)
			throws Exception;

	Map<String, Object> searchCustomerBeneficiary(String customerId)
			throws ApplicationException, BusinessException;
}