package com.gpt.product.gpcash.retail.transaction.purchasepayee.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.transaction.purchasepayee.model.CustomerPurchasePayeeModel;
import com.gpt.product.gpcash.retail.transaction.purchasepayee.repository.CustomerPurchasePayeeRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerPurchasePayeeServiceImpl implements CustomerPurchasePayeeService {
	@Autowired
	private CustomerPurchasePayeeRepository payeeRepo;

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;
	
	@Override
	public void checkPayee(String payeeName, String customerId) throws Exception {
		List<CustomerPurchasePayeeModel> modelList = payeeRepo.findByPayeeNameAndCustomerId(payeeName, customerId);

		if (modelList.size() != 0) {
			throw new BusinessException("GPT-0100118");
		}
				
	}

	@Override
	public void checkPayeeMustExist(String payeeName, String customerId) throws Exception {
		List<CustomerPurchasePayeeModel> modelList = payeeRepo.findByPayeeNameAndCustomerId(payeeName, customerId);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100119");
		}
	}

	@Override
	public void savePayee(String customerId, String payeeName, String description,
			String value1, String value2, String value3, String value4, String value5,String institutionCode, String createdBy)
			throws Exception {

		CustomerPurchasePayeeModel payee = new CustomerPurchasePayeeModel();
		payee.setPayeeName(payeeName);
		payee.setDescription(description);
		payee.setValue1(value1);
		payee.setValue2(value2);
		payee.setValue3(value3);
		payee.setValue4(value4);
		payee.setValue5(value5);
		
		PurchaseInstitutionModel institution = new PurchaseInstitutionModel();
		institution.setCode(institutionCode);
		payee.setPurchaseInstitution(institution);

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		payee.setCustomer(customer);

		savePayee(payee, createdBy);
	}

	private void savePayee(CustomerPurchasePayeeModel beneficiaryList, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		payeeRepo.persist(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchPayee(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.CUST_ID, customerId);
			Page<CustomerPurchasePayeeModel> result = payeeRepo.search(map, null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<CustomerPurchasePayeeModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerPurchasePayeeModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerPurchasePayeeModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("payeeName", model.getPayeeName());
		
		PurchaseInstitutionModel institution = model.getPurchaseInstitution();
		map.put("purchaseInstitutionCode", institution.getCode());
		map.put("purchaseInstitutionName", institution.getName());
		
		map.put("dscp", ValueUtils.getValue(model.getDescription()));
		map.put("value1", model.getValue1());
		map.put("value2", ValueUtils.getValue(model.getValue2()));
		map.put("value3", ValueUtils.getValue(model.getValue3()));
		map.put("value4", ValueUtils.getValue(model.getValue4()));
		map.put("value5", ValueUtils.getValue(model.getValue5()));

		return map;
	}
}
