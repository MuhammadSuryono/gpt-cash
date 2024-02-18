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

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListInHouseModel;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerBeneficiaryListInHouseServiceImpl implements CustomerBeneficiaryListInHouseService {

	@Autowired
	private CustomerBeneficiaryListInHouseRepository beneficiaryInHouseRepo;

	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;
	
    @Autowired
    private EAIEngine eaiAdapter;
    
    @Autowired
	private MaintenanceRepository maintenanceRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerBeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map,
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

	private List<Map<String, Object>> setModelToMap(List<CustomerBeneficiaryListInHouseModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerBeneficiaryListInHouseModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerBeneficiaryListInHouseModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.TRANS_BEN_ID, model.getId());
		map.put(ApplicationConstants.TRANS_BEN_ACCT, model.getBenAccountNo());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_NAME, model.getBenAccountName());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY, model.getBenAccountCurrency());

		map.put("isNotify", model.getIsNotifyBen());
		map.put("email", ValueUtils.getValue(model.getEmail()));
		
		String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
		CountryModel bankCountry = maintenanceRepo.isCountryValid(localCountryCode);
		map.put("bankCountryName", bankCountry.getName());

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

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_OVERBOOKING")) {

				vo.setAction("CREATE_OVERBOOKING");
				checkUniqueRecord(benAccountNo, customerId);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_OVERBOOKING")) {

				vo.setAction("UPDATE_OVERBOOKING");

				// check existing record exist or not
				CustomerBeneficiaryListInHouseModel beneficiaryInHouseOld = getExistingRecord(benAccountNo, customerId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryInHouseOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING")) {
				vo.setAction("DELETE_OVERBOOKING");

				// check existing record exist or not
				getExistingRecord(benAccountNo, customerId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING_LIST")) {
				vo.setAction("DELETE_OVERBOOKING_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId, true);
				}
				
			} else {
				throw new BusinessException("GPT-0100003");
			}

//			resultMap.putAll(pendingTaskService.savePendingTask(vo));
			pendingTaskService.savePendingTask(vo);
			resultMap.put(ApplicationConstants.PENDINGTASK_VO, vo);
			resultMap.putAll(map);
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public CustomerBeneficiaryListInHouseModel getExistingRecord(String accountNo, String customerId, boolean isThrowError) throws Exception {
		List<CustomerBeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCustomerId(accountNo, customerId);

		CustomerBeneficiaryListInHouseModel model = null;
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
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING_LIST")) {
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
		
		vo.setCreatedBy(customerId);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerBeneficiaryListSC");
		vo.setCustomerId(customerId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		return vo;
	}

	private CustomerBeneficiaryListInHouseModel setMapToModel(CustomerBeneficiaryListInHouseModel beneficiaryInHouse, Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		String isBenVA = ValueUtils.getValue((String)map.get("isBenVA"));

		beneficiaryInHouse.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryInHouse.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryInHouse.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryInHouse.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryInHouse.setEmail((String) map.get("email"));
		beneficiaryInHouse.setIsBenVirtualAccount(ApplicationConstants.NO);
		if (isBenVA.equals(ApplicationConstants.YES)) {
			beneficiaryInHouse.setIsBenVirtualAccount(ApplicationConstants.YES);
		}

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		beneficiaryInHouse.setCustomer(customer);

		return beneficiaryInHouse;
	}

	private void checkUniqueRecord(String accountNo, String customerId) throws Exception {
		List<CustomerBeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCustomerId(accountNo, customerId);

		if (modelList.size() > 0) {
			CustomerBeneficiaryListInHouseModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			if ("CREATE_OVERBOOKING".equals(vo.getAction())) {
				List<CustomerBeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
						.findByBenAccountNoAndCustomerId(benAccountNo, customerId);

				CustomerBeneficiaryListInHouseModel beneficiaryInHouseExisting = null;
				if (modelList.size() > 0) {
					beneficiaryInHouseExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryInHouseExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryInHouseExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryInHouseExisting, map);

					saveCustomerBeneficiaryInHouse(beneficiaryInHouseExisting, vo.getCreatedBy());
				} else {
					CustomerBeneficiaryListInHouseModel beneficiaryInHouse = new CustomerBeneficiaryListInHouseModel();
					setMapToModel(beneficiaryInHouse, map);

					saveCustomerBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
				}
			} else if ("UPDATE_OVERBOOKING".equals(vo.getAction())) {
				CustomerBeneficiaryListInHouseModel beneficiaryInHouseExisting = getExistingRecord(benAccountNo, customerId,
						true);

				setMapToModel(beneficiaryInHouseExisting, map);

				updateCustomerBeneficiaryInHouse(beneficiaryInHouseExisting, vo.getCreatedBy());
			} else if ("DELETE_OVERBOOKING".equals(vo.getAction())) {
				// check existing record exist or not
				CustomerBeneficiaryListInHouseModel beneficiaryInHouse = getExistingRecord(benAccountNo, customerId,
						true);

				deleteCustomerBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
			} else if("DELETE_OVERBOOKING_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					CustomerBeneficiaryListInHouseModel beneficiaryInHouse = getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId,
							true);

					deleteCustomerBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
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
	public void saveCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = beneficiaryList.getDeleteFlag() == null;
		beneficiaryList.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		if (isNew)
			beneficiaryInHouseRepo.persist(beneficiaryList);
		else
			beneficiaryInHouseRepo.save(beneficiaryList);
	}

	@Override
	public void updateCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryInHouseRepo.save(beneficiaryList);
	}

	@Override
	public void deleteCustomerBeneficiaryInHouse(CustomerBeneficiaryListInHouseModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
		beneficiaryList.setDeleteFlag(ApplicationConstants.YES);
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(deletedBy);

		beneficiaryInHouseRepo.save(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", map.get("benAccountNo"));
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
			
			Map<String, Object> result = new HashMap<>(12,1);
			result.put("benAccountNo", outputs.get("accountNo"));
			result.put("benAccountName", outputs.get("accountName"));
			result.put("benAccountCurrency", outputs.get("accountCurrencyCode"));
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchCustomerBeneficiary(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("customerId", customerId);
			Page<CustomerBeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map,
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
	public void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String createdBy, String isVirtualAccount) throws Exception {
		
		List<CustomerBeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCustomerId(benAccountNo, customerId);
		
		//request by Susi, jika sewaktu release udah ada, maka data pakai yg lama
		if(modelList.size() == 0) {
			CustomerBeneficiaryListInHouseModel beneficiaryListInHouse = new CustomerBeneficiaryListInHouseModel();

			beneficiaryListInHouse.setBenAccountNo(benAccountNo);
			beneficiaryListInHouse.setBenAccountName(benAccountName);
			beneficiaryListInHouse.setBenAccountCurrency(benAccountCurrency);
			beneficiaryListInHouse.setIsNotifyBen(isNotifyFlag);
			beneficiaryListInHouse.setEmail(email);
			beneficiaryListInHouse.setIsBenVirtualAccount(isVirtualAccount);

			CustomerModel customer = new CustomerModel();
			customer.setId(customerId);
			beneficiaryListInHouse.setCustomer(customer);

			saveCustomerBeneficiaryInHouse(beneficiaryListInHouse, createdBy);
		}
		//------------------------------------------------
	}

	@Override
	public Map<String, Object> searchBeneficiaryGroup(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			Map<String, Object> map = new HashMap<>();
			map.put("customerId", customerId);
			Page<CustomerBeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map, null);

			resultMap.put("result", setModelToMapGroup(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private Map<String, Object> setModelToMapGroup(List<CustomerBeneficiaryListInHouseModel> list) throws Exception {
		List<Map<String, Object>> listOtherAccount = new ArrayList<>();
		List<Map<String, Object>> listVirtualAccount = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<>();
		
		for (CustomerBeneficiaryListInHouseModel model : list) {
			if (model.getIsBenVirtualAccount() !=null && model.getIsBenVirtualAccount().equals(ApplicationConstants.YES)) {
				listVirtualAccount.add(setModelToMap(model));
			} else {
				listOtherAccount.add(setModelToMap(model));
			}
		}
		returnMap.put("Other Account", listOtherAccount);
		returnMap.put("Virtual Account", listVirtualAccount);
		
		return returnMap;
	}

	@Override
	public Map<String, Object> inquiryVirtualAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", map.get("benAccountNo"));
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSFER_VA_INQUIRY, inputs);
			
			Map<String, Object> result = new HashMap<>(12,1);
			result.put("benAccountNo", outputs.get("accountNo"));
			result.put("benAccountName", outputs.get("accountName"));
			result.put("benAccountCurrency", outputs.get("accountCurrencyCode"));
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}