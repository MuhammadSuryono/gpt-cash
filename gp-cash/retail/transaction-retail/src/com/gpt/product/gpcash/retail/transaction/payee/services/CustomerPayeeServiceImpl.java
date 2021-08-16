package com.gpt.product.gpcash.retail.transaction.payee.services;

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
import com.gpt.product.gpcash.biller.institution.model.InstitutionModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.transaction.payee.model.CustomerPayeeModel;
import com.gpt.product.gpcash.retail.transaction.payee.repository.CustomerPayeeRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerPayeeServiceImpl implements CustomerPayeeService {
	@Autowired
	private CustomerPayeeRepository payeeRepo;

	@Override
	public void checkCustomerPayee(String payeeName, String customerId) throws Exception {
		List<CustomerPayeeModel> modelList = payeeRepo.findByPayeeNameAndCustomerId(payeeName, customerId);

		if (modelList.size() != 0) {
			throw new BusinessException("GPT-0100118");
		}
				
	}

	@Override
	public void checkCustomerPayeeMustExist(String payeeName, String customerId) throws Exception {
		List<CustomerPayeeModel> modelList = payeeRepo.findByPayeeNameAndCustomerId(payeeName, customerId);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100119");
		}
	}

	@Override
	public void saveCustomerPayee(String customerId, String payeeName, String description,
			String value1, String value2, String value3, String value4, String value5,String institutionCode, String createdBy)
			throws Exception {

		CustomerPayeeModel payee = new CustomerPayeeModel();
		payee.setPayeeName(payeeName);
		payee.setDescription(description);
		payee.setValue1(value1);
		payee.setValue2(value2);
		payee.setValue3(value3);
		payee.setValue4(value4);
		payee.setValue5(value5);
		
		InstitutionModel institution = new InstitutionModel();
		institution.setCode(institutionCode);
		payee.setInstitution(institution);

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		payee.setCustomer(customer);

		saveCustomerPayee(payee, createdBy);
	}

	private void saveCustomerPayee(CustomerPayeeModel beneficiaryList, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		payeeRepo.persist(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchCustomerPayee(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.CUST_ID, customerId);
			Page<CustomerPayeeModel> result = payeeRepo.search(map, null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<CustomerPayeeModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerPayeeModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerPayeeModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("payeeName", model.getPayeeName());
		
		InstitutionModel institution = model.getInstitution();
		map.put("institutionCode", institution.getCode());
		map.put("institutionName", institution.getName());
		
		map.put("dscp", ValueUtils.getValue(model.getDescription()));
		map.put("value1", model.getValue1());
		map.put("value2", ValueUtils.getValue(model.getValue2()));
		map.put("value3", ValueUtils.getValue(model.getValue3()));
		map.put("value4", ValueUtils.getValue(model.getValue4()));
		map.put("value5", ValueUtils.getValue(model.getValue5()));

		return map;
	}
}
