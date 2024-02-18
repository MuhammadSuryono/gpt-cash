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
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerBeneficiaryListDomesticServiceImpl implements CustomerBeneficiaryListDomesticService {

	@Autowired
	private CustomerBeneficiaryListDomesticRepository beneficiaryDomesticRepo;

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;

	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
    @Autowired
    private EAIEngine eaiAdapter;		

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerBeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map,
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

	private List<Map<String, Object>> setModelToMap(List<CustomerBeneficiaryListDomesticModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerBeneficiaryListDomesticModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerBeneficiaryListDomesticModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.TRANS_BEN_ID, model.getId());
		map.put(ApplicationConstants.TRANS_BEN_ACCT, model.getBenAccountNo());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_NAME, model.getBenAccountName());
		map.put(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY, model.getBenAccountCurrency());
		map.put("benAliasName", model.getBenAliasName());

		DomesticBankModel domesticBank = model.getBenDomesticBankCode();
		map.put("bankCode", domesticBank.getCode());
		map.put("onlineBankCode", ValueUtils.getValue(domesticBank.getOnlineBankCode()));
		map.put("memberCode", ValueUtils.getValue(domesticBank.getMemberCode()));
		
		map.put("bankName", domesticBank.getName());
		
		map.put("branchCode", ValueUtils.getValue(domesticBank.getOrganizationUnitCode()));
		map.put("branchName", ValueUtils.getValue(domesticBank.getOrganizationUnitName()));
		
		if (ApplicationConstants.NO.equals(model.getIsBenOnline())) {
			
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
			
			BeneficiaryTypeModel benType = model.getBenType();
			map.put("beneficiaryTypeCode", benType.getCode());
			map.put("beneficiaryTypeName", benType.getName());
		}
		
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

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC")) {
				checkCustomValidation(map);

				vo.setAction("CREATE_DOMESTIC");
				checkUniqueRecord(benAccountNo, customerId);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_DOMESTIC")) {
				checkCustomValidation(map);

				vo.setAction("UPDATE_DOMESTIC");

				// check existing record exist or not
				CustomerBeneficiaryListDomesticModel beneficiaryDomesticOld = getExistingRecord(benAccountNo, customerId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryDomesticOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC")) {
				vo.setAction("DELETE_DOMESTIC");

				// check existing record exist or not
				getExistingRecord(benAccountNo, customerId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_LIST")) {
				vo.setAction("DELETE_DOMESTIC_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId, true);
				}
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC_ONLINE")) {
				maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));

				vo.setAction("CREATE_DOMESTIC_ONLINE");
				checkUniqueRecordforOnline(benAccountNo, customerId);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_DOMESTIC_ONLINE")) {
				maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));

				vo.setAction("UPDATE_DOMESTIC_ONLINE");

				// check existing record exist or not
				CustomerBeneficiaryListDomesticModel beneficiaryDomesticOld = getExistingOnlineRecord(benAccountNo, customerId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryDomesticOld));
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_ONLINE")) {
				vo.setAction("DELETE_DOMESTIC_ONLINE");

				// check existing record exist or not
				getExistingOnlineRecord(benAccountNo, customerId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_ONLINE_LIST")) {
				vo.setAction("DELETE_DOMESTIC_ONLINE_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingOnlineRecord((String) benAccountMap.get("benAccountNo"), customerId, true);
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
	
	private void checkUniqueRecordforOnline(String benAccountNo, String customerId) throws Exception{
		List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCustomerIdAndIsBenOnline(benAccountNo, customerId, ApplicationConstants.YES);

		if (modelList.size() > 0) {
			CustomerBeneficiaryListDomesticModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));
			maintenanceRepo.isBeneficiaryTypeValid((String) map.get("beneficiaryTypeCode"));
			
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
	
	public CustomerBeneficiaryListDomesticModel getExistingOnlineRecord(String accountNo, String customerId, boolean isThrowError) throws Exception {
		List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCustomerIdAndIsBenOnline(accountNo, customerId, ApplicationConstants.YES);

		CustomerBeneficiaryListDomesticModel model = null;
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

	@Override
	public CustomerBeneficiaryListDomesticModel getExistingRecord(String accountNo, String customerId, boolean isThrowError) throws Exception {
		List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCustomerId(accountNo, customerId);

		CustomerBeneficiaryListDomesticModel model = null;
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
		CustomerModel maker = customerUtilsRepo.isCustomerValid(customerId);
				
		CustomerUserPendingTaskVO vo = new CustomerUserPendingTaskVO();
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCustomerId(customerId);
		
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_LIST")) {
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
		
		vo.setCreatedBy((String) map.get(ApplicationConstants.CUST_ID));
		vo.setService("CustomerBeneficiaryListSC");
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		return vo;
	}

	private CustomerBeneficiaryListDomesticModel setMapToModel(CustomerBeneficiaryListDomesticModel beneficiaryDomestic, Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		CustomerModel corpUser = customerUtilsRepo.isCustomerValid(customerId);

		beneficiaryDomestic.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryDomestic.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryDomestic.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryDomestic.setBenAliasName((String) map.get("benAliasName"));
		beneficiaryDomestic.setBenAddr1((String) map.get("address1"));
		beneficiaryDomestic.setBenAddr2((String) map.get("address2"));
		beneficiaryDomestic.setBenAddr3((String) map.get("address3"));
		
		beneficiaryDomestic.setLldIsBenResidence((String) map.get("isBenResident"));
		if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenResidence())){
			CountryModel benResidentCountry = new CountryModel();
			benResidentCountry.setCode((String) map.get("benResidentCountryCode"));
			beneficiaryDomestic.setLldBenResidenceCountry(benResidentCountry);
		}
		
		beneficiaryDomestic.setLldIsBenCitizen((String) map.get("isBenCitizen"));
		if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenCitizen())){
			CountryModel benCitizenCountry = new CountryModel();
			benCitizenCountry.setCode((String) map.get("benCitizenCountryCode"));
			beneficiaryDomestic.setLldBenCitizenCountry(benCitizenCountry);
		}
		
		BeneficiaryTypeModel beneficiaryType = new BeneficiaryTypeModel();
		beneficiaryType.setCode((String)map.get("beneficiaryTypeCode"));
		beneficiaryDomestic.setBenType(beneficiaryType);
		
		DomesticBankModel domBank = new DomesticBankModel();
		domBank.setCode((String) map.get("bankCode"));
		beneficiaryDomestic.setBenDomesticBankCode(domBank);
		
		beneficiaryDomestic.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryDomestic.setEmail((String) map.get("email"));

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		beneficiaryDomestic.setCustomer(customer);
		
		beneficiaryDomestic.setIsBenOnline(ApplicationConstants.NO);
		
		return beneficiaryDomestic;
	}

	private void checkUniqueRecord(String accountNo, String customerId) throws Exception {
		List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCustomerId(accountNo, customerId);

		if (modelList.size() > 0) {
			CustomerBeneficiaryListDomesticModel model = modelList.get(0);
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

			if ("CREATE_DOMESTIC".equals(vo.getAction())) {
				List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
						.findByBenAccountNoAndCustomerId(benAccountNo, customerId);

				CustomerBeneficiaryListDomesticModel beneficiaryDomesticExisting = null;
				if (modelList.size() > 0) {
					beneficiaryDomesticExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryDomesticExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryDomesticExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryDomesticExisting, map);

					saveCustomerBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
				} else {
					CustomerBeneficiaryListDomesticModel beneficiaryDomestic = new CustomerBeneficiaryListDomesticModel();
					setMapToModel(beneficiaryDomestic, map);

					saveCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("UPDATE_DOMESTIC".equals(vo.getAction())) {
				CustomerBeneficiaryListDomesticModel beneficiaryDomesticExisting = getExistingRecord(benAccountNo, customerId,
						true);

				setMapToModel(beneficiaryDomesticExisting, map);

				updateCustomerBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
			} else if ("DELETE_DOMESTIC".equals(vo.getAction())) {
				// check existing record exist or not
				CustomerBeneficiaryListDomesticModel beneficiaryDomestic = getExistingRecord(benAccountNo, customerId,
						true);

				deleteCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
			} else if("DELETE_DOMESTIC_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					CustomerBeneficiaryListDomesticModel beneficiaryDomestic = getExistingRecord((String) benAccountMap.get("benAccountNo"), customerId,
							true);

					deleteCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("CREATE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCustomerIdAndIsBenOnline(benAccountNo, customerId, ApplicationConstants.YES);

				CustomerBeneficiaryListDomesticModel beneficiaryDomesticExisting = null;
				if (modelList.size() > 0) {
					beneficiaryDomesticExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryDomesticExisting != null && ApplicationConstants.YES.equals(beneficiaryDomesticExisting.getDeleteFlag())) {
					setMapToModelOnline(beneficiaryDomesticExisting, map);

					saveCustomerBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
				} else {
					CustomerBeneficiaryListDomesticModel beneficiaryDomestic = new CustomerBeneficiaryListDomesticModel();
					setMapToModelOnline(beneficiaryDomestic, map);

					saveCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("UPDATE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				CustomerBeneficiaryListDomesticModel beneficiaryDomesticExisting = getExistingOnlineRecord(benAccountNo, customerId, true);

				setMapToModelOnline(beneficiaryDomesticExisting, map);

				updateCustomerBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
			} else if ("DELETE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				// check existing record exist or not
				CustomerBeneficiaryListDomesticModel beneficiaryDomestic = getExistingOnlineRecord(benAccountNo, customerId, true);

				deleteCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
			} else if("DELETE_DOMESTIC_ONLINE_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					CustomerBeneficiaryListDomesticModel beneficiaryDomestic = getExistingOnlineRecord((String) benAccountMap.get("benAccountNo"), customerId, true);

					deleteCustomerBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} 
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private CustomerBeneficiaryListDomesticModel setMapToModelOnline(CustomerBeneficiaryListDomesticModel beneficiaryDomestic, Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		CustomerModel corpUser = customerUtilsRepo.isCustomerValid(customerId);

		beneficiaryDomestic.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryDomestic.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryDomestic.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryDomestic.setBenAliasName((String) map.get("benAliasName"));
		
		DomesticBankModel domBank = new DomesticBankModel();
		domBank.setCode((String) map.get("bankCode"));
		beneficiaryDomestic.setBenDomesticBankCode(domBank);
		
		beneficiaryDomestic.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryDomestic.setEmail((String) map.get("email"));

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		beneficiaryDomestic.setCustomer(customer);
		
		beneficiaryDomestic.setIsBenOnline(ApplicationConstants.YES);

		return beneficiaryDomestic;
		
	}

	@Override
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = beneficiaryList.getDeleteFlag() == null;
		beneficiaryList.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryList.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setCreatedBy(createdBy);
		beneficiaryList.setUpdatedDate(null);
		beneficiaryList.setUpdatedBy(null);

		if (isNew) {
			beneficiaryDomesticRepo.persist(beneficiaryList);
		} else
			beneficiaryDomesticRepo.save(beneficiaryList);
	}

	@Override
	public void updateCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryDomesticRepo.save(beneficiaryList);
	}

	@Override
	public void deleteCustomerBeneficiaryDomestic(CustomerBeneficiaryListDomesticModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
		beneficiaryList.setDeleteFlag(ApplicationConstants.YES);
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(deletedBy);

		beneficiaryDomesticRepo.save(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			DomesticBankModel domBank = maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));
			inputs.put("accountNo", map.get("benAccountNo"));
			inputs.put("onlineBankCode", domBank.getOnlineBankCode());
			inputs.put("chargeTo", map.get("chargeTo"));
			inputs.put("channel", domBank.getChannel());
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.DOMESTIC_ONLINE_ACCOUNT_INQUIRY, inputs);
			
			Map<String, Object> result = new HashMap<>();
			result.put("benAccountNo", outputs.get("accountNo"));
			result.put("benAccountName", outputs.get("accountName"));
			
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
			
			Page<CustomerBeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map,
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
	public CustomerBeneficiaryListDomesticModel findCustomerBeneficiary(String benId) throws Exception {
		CustomerBeneficiaryListDomesticModel model = beneficiaryDomesticRepo.findOne(benId);

		if (model != null && ApplicationConstants.YES.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}

	@Override
	public void saveCustomerBeneficiary(String customerId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode,
			String bankCode, String createdBy, boolean isBenOnline) throws Exception {
		
		
		List<CustomerBeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCustomerId(benAccountNo, customerId);
		
		if(modelList.size() == 0) {
			CustomerBeneficiaryListDomesticModel beneficiaryDomestic = new CustomerBeneficiaryListDomesticModel();

			beneficiaryDomestic.setBenAccountNo(benAccountNo);
			beneficiaryDomestic.setBenAccountName(benAccountName);
			beneficiaryDomestic.setBenAccountCurrency(benAccountCurrency);
			beneficiaryDomestic.setIsNotifyBen(isNotifyFlag);
			beneficiaryDomestic.setEmail(email);

			CustomerModel customer = new CustomerModel();
			customer.setId(customerId);
			beneficiaryDomestic.setCustomer(customer);

			beneficiaryDomestic.setBenAliasName(benAliasName);
			beneficiaryDomestic.setBenAddr1(address1);
			beneficiaryDomestic.setBenAddr2(address2);
			beneficiaryDomestic.setBenAddr3(address3);
			if (isBenOnline) {
				beneficiaryDomestic.setIsBenOnline(ApplicationConstants.YES);
			} else {
				beneficiaryDomestic.setIsBenOnline(ApplicationConstants.NO);
				beneficiaryDomestic.setLldIsBenResidence(isBenResident);
				
				String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
				
				if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenResidence())){
					benResidentCountryCode = localCountryCode;
				}
				CountryModel benResidentCountry = maintenanceRepo.isCountryValid(benResidentCountryCode);
				beneficiaryDomestic.setLldBenResidenceCountry(benResidentCountry);
				
				beneficiaryDomestic.setLldIsBenCitizen(isBenCitizen);
				if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenCitizen())){
					benCitizenCountryCode = localCountryCode;
				}
				CountryModel benCitizenCountry = maintenanceRepo.isCountryValid(benCitizenCountryCode);
				beneficiaryDomestic.setLldBenCitizenCountry(benCitizenCountry);
				
				BeneficiaryTypeModel beneficiaryType = new BeneficiaryTypeModel();
				beneficiaryType.setCode(beneficiaryTypeCode);
				beneficiaryDomestic.setBenType(beneficiaryType);
			}	
			
			DomesticBankModel domBank = new DomesticBankModel();
			domBank.setCode(bankCode);
			beneficiaryDomestic.setBenDomesticBankCode(domBank);
			
			
			saveCustomerBeneficiaryDomestic(beneficiaryDomestic, createdBy);
		}
		//-------------------------------
	}

	@Override
	public Map<String, Object> searchOnlineBeneficiary(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("customerId", customerId);
			map.put("isOnline", ApplicationConstants.YES);
			Page<CustomerBeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map, null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

}
