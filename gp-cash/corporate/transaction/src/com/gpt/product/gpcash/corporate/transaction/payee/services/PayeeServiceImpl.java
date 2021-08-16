package com.gpt.product.gpcash.corporate.transaction.payee.services;

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
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.corporate.transaction.payee.model.PayeeModel;
import com.gpt.product.gpcash.corporate.transaction.payee.repository.PayeeRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class PayeeServiceImpl implements PayeeService {
	@Autowired
	private PayeeRepository payeeRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Override
	public void checkPayee(String payeeName, String userGroupId) throws Exception {
		List<PayeeModel> modelList = payeeRepo.findByPayeeNameAndCorporateUserGroupId(payeeName, userGroupId);

		if (modelList.size() != 0) {
			throw new BusinessException("GPT-0100118");
		}
				
	}

	@Override
	public void checkPayeeMustExist(String payeeName, String userGroupId) throws Exception {
		List<PayeeModel> modelList = payeeRepo.findByPayeeNameAndCorporateUserGroupId(payeeName, userGroupId);

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100119");
		}
	}

	@Override
	public void savePayee(String corporateId, String corporateUserGroupId, String payeeName, String description,
			String value1, String value2, String value3, String value4, String value5,String institutionCode, String createdBy)
			throws Exception {

		PayeeModel payee = new PayeeModel();
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

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		payee.setCorporate(corporate);

		CorporateUserGroupModel corporateUserGroup = new CorporateUserGroupModel();
		corporateUserGroup.setId(corporateUserGroupId);
		payee.setCorporateUserGroup(corporateUserGroup);

		savePayee(payee, createdBy);
	}

	private void savePayee(PayeeModel beneficiaryList, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		payeeRepo.persist(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchPayee(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.CORP_ID, corporateId);
			map.put("corporateUserGroupId", corporateUser.getCorporateUserGroup().getId());
			Page<PayeeModel> result = payeeRepo.search(map, null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<PayeeModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PayeeModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(PayeeModel model) {
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
