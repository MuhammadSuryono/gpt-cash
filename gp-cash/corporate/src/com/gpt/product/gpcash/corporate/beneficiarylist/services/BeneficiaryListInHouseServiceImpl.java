package com.gpt.product.gpcash.corporate.beneficiarylist.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BeneficiaryListInHouseServiceImpl implements BeneficiaryListInHouseService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BeneficiaryListInHouseRepository beneficiaryInHouseRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
    @Autowired
    private EAIEngine eaiAdapter;
    
    @Autowired
	private MaintenanceRepository maintenanceRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map,
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

	private List<Map<String, Object>> setModelToMap(List<BeneficiaryListInHouseModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BeneficiaryListInHouseModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(BeneficiaryListInHouseModel model) throws Exception {
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
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_OVERBOOKING")) {

				vo.setAction("CREATE_OVERBOOKING");
				checkUniqueRecord(benAccountNo, corporateId);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_OVERBOOKING")) {

				vo.setAction("UPDATE_OVERBOOKING");

				// check existing record exist or not
				BeneficiaryListInHouseModel beneficiaryInHouseOld = getExistingRecord(benAccountNo, corporateId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryInHouseOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING")) {
				vo.setAction("DELETE_OVERBOOKING");

				// check existing record exist or not
				getExistingRecord(benAccountNo, corporateId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING_LIST")) {
				vo.setAction("DELETE_OVERBOOKING_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingRecord((String) benAccountMap.get("benAccountNo"), corporateId, true);
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

	@Override
	public BeneficiaryListInHouseModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception {
		List<BeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		BeneficiaryListInHouseModel model = null;
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

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCorporateId(corporateId);
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_OVERBOOKING_LIST")) {
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>) map.get("benAccountList");
			for(Map<String, Object> benAccountMap : benAccountList){
				String uniqueKey = corporateId.concat((String) benAccountMap.get("benAccountNo"));
				uniqueKeyAppend = uniqueKeyAppend + uniqueKey + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(uniqueKey);
				pendingTaskService.checkUniquePendingTaskLike(vo);
			}
			
//			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
		} else {
			String uniqueKey = corporateId.concat((String) map.get("benAccountNo"));
			
			vo.setUniqueKey(uniqueKey);
			vo.setUniqueKeyDisplay((String) map.get("benAccountNo"));
			
			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
		}
		
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setService("BeneficiaryListSC");
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		//set userGroupId
		vo.setUserGroupId(userGroupId);

		return vo;
	}

	private BeneficiaryListInHouseModel setMapToModel(BeneficiaryListInHouseModel beneficiaryInHouse, Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);
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

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		beneficiaryInHouse.setCorporate(corporate);

		beneficiaryInHouse.setCorporateUserGroup(corpUser.getCorporateUserGroup());

		return beneficiaryInHouse;
	}

	private void checkUniqueRecord(String accountNo, String corporateId) throws Exception {
		List<BeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		if (modelList.size() > 0) {
			BeneficiaryListInHouseModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			if ("CREATE_OVERBOOKING".equals(vo.getAction())) {
				List<BeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
						.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);

				BeneficiaryListInHouseModel beneficiaryInHouseExisting = null;
				if (modelList.size() > 0) {
					beneficiaryInHouseExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryInHouseExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryInHouseExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryInHouseExisting, map);

					saveBeneficiaryInHouse(beneficiaryInHouseExisting, vo.getCreatedBy());
				} else {
					BeneficiaryListInHouseModel beneficiaryInHouse = new BeneficiaryListInHouseModel();
					setMapToModel(beneficiaryInHouse, map);

					saveBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
				}
			} else if ("UPDATE_OVERBOOKING".equals(vo.getAction())) {
				BeneficiaryListInHouseModel beneficiaryInHouseExisting = getExistingRecord(benAccountNo, corporateId,
						true);

				setMapToModel(beneficiaryInHouseExisting, map);

				updateBeneficiaryInHouse(beneficiaryInHouseExisting, vo.getCreatedBy());
			} else if ("DELETE_OVERBOOKING".equals(vo.getAction())) {
				// check existing record exist or not
				BeneficiaryListInHouseModel beneficiaryInHouse = getExistingRecord(benAccountNo, corporateId,
						true);

				deleteBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
			} else if("DELETE_OVERBOOKING_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					BeneficiaryListInHouseModel beneficiaryInHouse = getExistingRecord((String) benAccountMap.get("benAccountNo"), corporateId,
							true);

					deleteBeneficiaryInHouse(beneficiaryInHouse, vo.getCreatedBy());
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
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
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
	public void updateBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryInHouseRepo.save(beneficiaryList);
	}

	@Override
	public void deleteBeneficiaryInHouse(BeneficiaryListInHouseModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
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
	public Map<String, Object> searchBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			Map<String, Object> map = new HashMap<>();
			map.put("corporateId", corporateId);
			
			// Requested by Susi (10-10-2018), Scope for Beneficiary is Corporate
//			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
//			map.put("corporateUserGroupId", corporateUser.getCorporateUserGroup().getId()); 
			Page<BeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map,
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
	public void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String createdBy, String isVirtualAccount) throws Exception {
		
		List<BeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);
		
		//request by Susi, jika sewaktu release udah ada, maka data pakai yg lama
		if(modelList.size() == 0) {
			BeneficiaryListInHouseModel beneficiaryListInHouse = new BeneficiaryListInHouseModel();

			beneficiaryListInHouse.setBenAccountNo(benAccountNo);
			beneficiaryListInHouse.setBenAccountName(benAccountName);
			beneficiaryListInHouse.setBenAccountCurrency(benAccountCurrency);
			beneficiaryListInHouse.setIsNotifyBen(isNotifyFlag);
			beneficiaryListInHouse.setEmail(email);
			beneficiaryListInHouse.setIsBenVirtualAccount(isVirtualAccount);

			CorporateModel corporate = new CorporateModel();
			corporate.setId(corporateId);
			beneficiaryListInHouse.setCorporate(corporate);

			CorporateUserGroupModel corporateUserGroup = new CorporateUserGroupModel();
			corporateUserGroup.setId(corporateUserGroupId);
			beneficiaryListInHouse.setCorporateUserGroup(corporateUserGroup);

			saveBeneficiaryInHouse(beneficiaryListInHouse, createdBy);
		}
		//------------------------------------------------
	}

	@Override
	public Map<String, Object> searchBeneficiaryGroup(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			Map<String, Object> map = new HashMap<>();
			map.put("corporateId", corporateId);
			
			// Requested by Susi (10-10-2018), Scope for Beneficiary is Corporate
//			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
//			map.put("corporateUserGroupId", corporateUser.getCorporateUserGroup().getId()); 
			Page<BeneficiaryListInHouseModel> result = beneficiaryInHouseRepo.search(map, null);

			resultMap.put("result", setModelToMapGroup(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private Map<String, Object> setModelToMapGroup(List<BeneficiaryListInHouseModel> list) throws Exception {
		List<Map<String, Object>> listOtherAccount = new ArrayList<>();
		List<Map<String, Object>> listVirtualAccount = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<>();
		
		for (BeneficiaryListInHouseModel model : list) {
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