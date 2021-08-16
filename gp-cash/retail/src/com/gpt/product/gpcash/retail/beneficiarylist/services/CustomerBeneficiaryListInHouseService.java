package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInHouseModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;

@AutoDiscoveryImpl
public interface CustomerBeneficiaryListInHouseService extends CustomerUserWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	void saveCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException;
	
	void updateCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException;
	
	void deleteCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String deletedBy) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchCustomerBeneficiary(String customerId) throws ApplicationException, BusinessException;
	
	void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String createdBy) throws Exception;

	CustomerBeneficiaryListInHouseModel getExistingRecord(String accountNo, String customerId, boolean isThrowError)
			throws Exception;
}
