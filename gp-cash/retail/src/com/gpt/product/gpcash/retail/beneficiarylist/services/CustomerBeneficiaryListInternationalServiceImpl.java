package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.math.BigDecimal;
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
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInternationalModel;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerBeneficiaryListInternationalServiceImpl implements CustomerBeneficiaryListInternationalService {

	@Autowired
	private CustomerBeneficiaryListInternationalRepository beneficiaryInternationalRepo;

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;

	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
		

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerBeneficiaryListInternationalModel> result = beneficiaryInternationalRepo.search(map,
					PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent()));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CustomerBeneficiaryListInternationalModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerBeneficiaryListInternationalModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerBeneficiaryListInternationalModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.TRANS_BEN_ID, model.getId());
		map.put(ApplicationConstants.TRANS_BEN_ACCT, model.getBenAccountNo());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_NAME, model.getBenAccountName());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY, model.getBenAccountCurrency());
		map.put("benAliasName", model.getBenAliasName());

		InternationalBankModel internationalBank = model.getBenInternationalBankCode();
		map.put("bankCode", internationalBank.getCode());
		map.put("bankName", internationalBank.getName());
		map.put("bankCountryCode", internationalBank.getCountry().getCode());
		map.put("bankCountryName", internationalBank.getCountry().getName());
		map.put("bankBranchName", ValueUtils.getValue(internationalBank.getOrganizationUnitName()));
		map.put("bankAddress1", ValueUtils.getValue(internationalBank.getAddress1()));
		map.put("bankAddress2", ValueUtils.getValue(internationalBank.getAddress2()));
		map.put("bankAddress3", ValueUtils.getValue(internationalBank.getAddress3()));
		
		map.put("address1", ValueUtils.getValue(model.getBenAddr1()));
		map.put("address2", ValueUtils.getValue(model.getBenAddr2()));
		map.put("address3", ValueUtils.getValue(model.getBenAddr3()));
		
		map.put("isBenResident", ValueUtils.getValue(model.getLldIsBenResidence()));
		CountryModel benResidentCountry = model.getLldBenResidenceCountry();
		map.put("benResidentCountryCode", benResidentCountry.getCode());
		map.put("benResidentCountryName", benResidentCountry.getName());
		
		map.put("isBenCitizen", ValueUtils.getValue(model.getLldIsBenCitizen()));
		CountryModel benCitizenCountry = model.getLldBenCitizenCountry();
		map.put("benCitizenCountryCode", benCitizenCountry.getCode());
		map.put("benCitizenCountryName", benCitizenCountry.getName());
		
		map.put("isNotify", model.getIsNotifyBen());
		map.put("email", ValueUtils.getValue(model.getEmail()));
		
		map.put("isBenAffiliated", ValueUtils.getValue(model.getLldIsBenAffiliated()));
		map.put("isBenIdentical", ValueUtils.getValue(model.getLldIsBenIdentical()));

		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			CustomerUserPendingTaskVO vo = setCustomerUserPendingTaskVO(map);
			vo.setJsonObject(map);

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_INTERNATIONAL")) {
				checkCustomValidation(map);

				vo.setAction("CREATE_INTERNATIONAL");
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_INTERNATIONAL")) {
				checkCustomValidation(map);

				vo.setAction("UPDATE_INTERNATIONAL");

				// check existing record exist or not
				CustomerBeneficiaryListInternationalModel beneficiaryInternationalOld = getExistingRecord(benAccountNo, customerId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryInternationalOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL")) {
				vo.setAction("DELETE_INTERNATIONAL");

				// check existing record exist or not
				getExistingRecord(benAccountNo, customerId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL_LIST")) {
				vo.setAction("DELETE_INTERNATIONAL_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId, true);
				}
				
			} else {
				throw new BusinessException("GPT-0100003");
			}

			resultMap.putAll(pendingTaskService.savePendingTask(vo));
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isInternationalBankValid((String) map.get("bankCode"));
			
			if(ApplicationConstants.YES.equals((String) map.get("isBenResident"))){
				if(ValueUtils.hasValue(map.get("benResidentCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benResidentCountryCode"));
				} else {
					throw new BusinessException("GPT-0100089");
				}
			}
			
			if(ApplicationConstants.YES.equals((String) map.get("isBenCitizen"))){
				if(ValueUtils.hasValue(map.get("benCitizenCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benCitizenCountryCode"));
				} else {
					throw new BusinessException("GPT-0100090");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public CustomerBeneficiaryListInternationalModel getExistingRecord(String accountNo, String customerId, boolean isThrowError) throws Exception {
		List<CustomerBeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
				.findByBenAccountNoAndCustomerId(accountNo, customerId);

		CustomerBeneficiaryListInternationalModel model = null;
		if (modelList.size() == 0) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			model = modelList.get(0);
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}

		return model;
	}

	private CustomerUserPendingTaskVO setCustomerUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
				
		CustomerUserPendingTaskVO vo = new CustomerUserPendingTaskVO();
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL_LIST")) {
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>) map.get("benAccountList");
			for(Map<String, Object> benAccountMap : benAccountList){
				String uniqueKey = customerId.concat((String) benAccountMap.get("benAccountNo"));
				uniqueKeyAppend = uniqueKeyAppend + uniqueKey + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(uniqueKey);
				pendingTaskService.checkUniquePendingTaskLike(vo);
			}
			
			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.CUST_ID));
		} else {
			String uniqueKey = customerId.concat((String) map.get("benAccountNo"));
			
			vo.setUniqueKey(uniqueKey);
			vo.setUniqueKeyDisplay((String) map.get("benAccountNo"));
			
			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
		}
		
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerBeneficiaryListSC");
		vo.setCustomerId(customerId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		return vo;
	}

	private CustomerBeneficiaryListInternationalModel setMapToModel(CustomerBeneficiaryListInternationalModel beneficiaryInternational, Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CustomerModel custUser = customerUtilsRepo.isCustomerValid(userCode);
		String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();

		beneficiaryInternational.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryInternational.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryInternational.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryInternational.setBenAliasName((String) map.get("benAliasName"));
		beneficiaryInternational.setBenAddr1((String) map.get("address1"));
		beneficiaryInternational.setBenAddr2((String) map.get("address2"));
		beneficiaryInternational.setBenAddr3((String) map.get("address3"));
		
		beneficiaryInternational.setLldIsBenResidence((String) map.get("isBenResident"));
		CountryModel benResidentCountry = new CountryModel();
		benResidentCountry.setCode(localCountryCode);
		
		if(ApplicationConstants.NO.equals(beneficiaryInternational.getLldIsBenResidence())){
			benResidentCountry.setCode((String) map.get("benResidentCountryCode"));
		}
		beneficiaryInternational.setLldBenResidenceCountry(benResidentCountry);
		
		beneficiaryInternational.setLldIsBenCitizen((String) map.get("isBenCitizen"));
		CountryModel benCitizenCountry = new CountryModel();
		benCitizenCountry.setCode(localCountryCode);
		
		if(ApplicationConstants.NO.equals(beneficiaryInternational.getLldIsBenCitizen())){
			benCitizenCountry.setCode((String) map.get("benCitizenCountryCode"));
		}
		beneficiaryInternational.setLldBenCitizenCountry(benCitizenCountry);
		
		beneficiaryInternational.setLldIsBenAffiliated((String) map.get("isBenAffiliated"));
		beneficiaryInternational.setLldIsBenIdentical((String) map.get("isBenIdentical"));
		
		InternationalBankModel intBank = new InternationalBankModel();
		intBank.setCode((String) map.get("bankCode"));
		beneficiaryInternational.setBenInternationalBankCode(intBank);
		
		CountryModel bankCountryCode = new CountryModel();
		bankCountryCode.setCode((String) map.get("intCountryCode"));
		beneficiaryInternational.setBenInternationalCountry(bankCountryCode);
		
		beneficiaryInternational.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryInternational.setEmail((String) map.get("email"));

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		beneficiaryInternational.setCustomer(customer);

		return beneficiaryInternational;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			if ("CREATE_INTERNATIONAL".equals(vo.getAction())) {
				List<CustomerBeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
						.findByBenAccountNoAndCustomerId(benAccountNo, customerId);

				CustomerBeneficiaryListInternationalModel beneficiaryInternationalExisting = null;
				if (modelList.size() > 0) {
					beneficiaryInternationalExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryInternationalExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryInternationalExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryInternationalExisting, map);

					saveCustomerBeneficiaryInternational(beneficiaryInternationalExisting, vo.getCreatedBy());
				} else {
					CustomerBeneficiaryListInternationalModel beneficiaryInternational = new CustomerBeneficiaryListInternationalModel();
					setMapToModel(beneficiaryInternational, map);

					saveCustomerBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
				}
			} else if ("UPDATE_INTERNATIONAL".equals(vo.getAction())) {
				CustomerBeneficiaryListInternationalModel beneficiaryInternationalExisting = getExistingRecord(benAccountNo, customerId,
						true);

				setMapToModel(beneficiaryInternationalExisting, map);

				updateCustomerBeneficiaryInternational(beneficiaryInternationalExisting, vo.getCreatedBy());
			} else if ("DELETE_INTERNATIONAL".equals(vo.getAction())) {
				// check existing record exist or not
				CustomerBeneficiaryListInternationalModel beneficiaryInternational = getExistingRecord(benAccountNo, customerId,
						true);

				deleteCustomerBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
			} else if("DELETE_INTERNATIONAL_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					CustomerBeneficiaryListInternationalModel beneficiaryInternational = getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId,
							true);

					deleteCustomerBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode,String isBenIdentity,
			String isBenAffiliated, String benCountry,String bankCode, String createdBy) throws Exception {
		
		
		List<CustomerBeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
				.findByBenAccountNoAndCustomerId(benAccountNo, customerId);
		
		//request by Susi, jika sewaktu release udah ada, maka data pakai yg lama
		if(modelList.size() == 0) {
			CustomerBeneficiaryListInternationalModel beneficiaryInternational = new CustomerBeneficiaryListInternationalModel();

			beneficiaryInternational.setBenAccountNo(benAccountNo);
			beneficiaryInternational.setBenAccountName(benAccountName);
			beneficiaryInternational.setBenAccountCurrency(benAccountCurrency);
			beneficiaryInternational.setIsNotifyBen(isNotifyFlag);
			beneficiaryInternational.setEmail(email);

			CustomerModel customer = new CustomerModel();
			customer.setId(customerId);
			beneficiaryInternational.setCustomer(customer);

			beneficiaryInternational.setBenAliasName(benAliasName);
			beneficiaryInternational.setBenAddr1(address1);
			beneficiaryInternational.setBenAddr2(address2);
			beneficiaryInternational.setBenAddr3(address3);
			
			beneficiaryInternational.setLldIsBenResidence(isBenResident);
			
			String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
			
			if(ApplicationConstants.YES.equals(beneficiaryInternational.getLldIsBenResidence())){
				benResidentCountryCode = localCountryCode;
			}
			CountryModel benResidentCountry = maintenanceRepo.isCountryValid(benResidentCountryCode);
			beneficiaryInternational.setLldBenResidenceCountry(benResidentCountry);
			
			beneficiaryInternational.setLldIsBenCitizen(isBenCitizen);
			if(ApplicationConstants.YES.equals(beneficiaryInternational.getLldIsBenCitizen())){
				benCitizenCountryCode = localCountryCode;
			}
			CountryModel benCitizenCountry = maintenanceRepo.isCountryValid(benCitizenCountryCode);
			beneficiaryInternational.setLldBenCitizenCountry(benCitizenCountry);
			
			InternationalBankModel intBank = new InternationalBankModel();
			intBank.setCode(bankCode);
			beneficiaryInternational.setBenInternationalBankCode(intBank);
			
			beneficiaryInternational.setLldIsBenIdentical(isBenIdentity);
			beneficiaryInternational.setLldIsBenAffiliated(isBenAffiliated);
			
			CountryModel benCountryModel = maintenanceRepo.isCountryValid(benCountry);
			beneficiaryInternational.setBenInternationalCountry(benCountryModel);
			
			saveCustomerBeneficiaryInternational(beneficiaryInternational, createdBy);
		}
		//-------------------------------
	}
	
	@Override
	public void saveCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = beneficiaryList.getDeleteFlag() == null;
		beneficiaryList.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		if (isNew) {
			beneficiaryInternationalRepo.persist(beneficiaryList);
		} else
			beneficiaryInternationalRepo.save(beneficiaryList);
	}

	@Override
	public void updateCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryInternationalRepo.save(beneficiaryList);
	}

	@Override
	public void deleteCustomerBeneficiaryInternational(CustomerBeneficiaryListInternationalModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
		beneficiaryList.setDeleteFlag(ApplicationConstants.YES);
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(deletedBy);

		beneficiaryInternationalRepo.save(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchCustomerBeneficiary(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("customerId", customerId);
			
			Page<CustomerBeneficiaryListInternationalModel> result = beneficiaryInternationalRepo.search(map,
					null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public CustomerBeneficiaryListInternationalModel findCustomerBeneficiary(String benId) throws Exception {
		CustomerBeneficiaryListInternationalModel model = beneficiaryInternationalRepo.findOne(benId);

		if (model != null && ApplicationConstants.YES.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}

}
