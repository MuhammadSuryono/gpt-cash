package com.gpt.product.gpcash.corporate.beneficiarylist.services;

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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BeneficiaryListDomesticServiceImpl implements BeneficiaryListDomesticService {

	@Autowired
	private BeneficiaryListDomesticRepository beneficiaryDomesticRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
    @Autowired
    private EAIEngine eaiAdapter;		

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map,
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

	private List<Map<String, Object>> setModelToMap(List<BeneficiaryListDomesticModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BeneficiaryListDomesticModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(BeneficiaryListDomesticModel model) throws Exception {
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

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC")) {
				checkCustomValidation(map);

				vo.setAction("CREATE_DOMESTIC");
				checkUniqueRecord(benAccountNo, corporateId);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_DOMESTIC")) {
				checkCustomValidation(map);

				vo.setAction("UPDATE_DOMESTIC");

				// check existing record exist or not
				BeneficiaryListDomesticModel beneficiaryDomesticOld = getExistingRecord(benAccountNo, corporateId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryDomesticOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC")) {
				vo.setAction("DELETE_DOMESTIC");

				// check existing record exist or not
				getExistingRecord(benAccountNo, corporateId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_LIST")) {
				vo.setAction("DELETE_DOMESTIC_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingRecord((String) benAccountMap.get("benAccountNo"), corporateId, true);
				}
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC_ONLINE")) {
				maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));

				vo.setAction("CREATE_DOMESTIC_ONLINE");
				checkUniqueRecordforOnline(benAccountNo, corporateId);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_DOMESTIC_ONLINE")) {
				maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));

				vo.setAction("UPDATE_DOMESTIC_ONLINE");

				// check existing record exist or not
				BeneficiaryListDomesticModel beneficiaryDomesticOld = getExistingOnlineRecord(benAccountNo, corporateId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryDomesticOld));
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_ONLINE")) {
				vo.setAction("DELETE_DOMESTIC_ONLINE");

				// check existing record exist or not
				getExistingOnlineRecord(benAccountNo, corporateId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_ONLINE_LIST")) {
				vo.setAction("DELETE_DOMESTIC_ONLINE_LIST");

				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					
					// check existing record exist or not
					getExistingOnlineRecord((String) benAccountMap.get("benAccountNo"), corporateId, true);
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
	
	private void checkUniqueRecordforOnline(String benAccountNo, String corporateId) throws Exception{
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCorporateIdAndIsBenOnline(benAccountNo, corporateId, ApplicationConstants.YES);

		if (modelList.size() > 0) {
			BeneficiaryListDomesticModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isDomesticBankValid((String) map.get("bankCode"));
			maintenanceRepo.isBeneficiaryTypeValid((String) map.get("beneficiaryTypeCode"));
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenResident"))){
				if(ValueUtils.hasValue(map.get("benResidentCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benResidentCountryCode"));
				} else {
					throw new BusinessException("GPT-0100089");
				}
			}
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenCitizen"))){
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
	public BeneficiaryListDomesticModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception {
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		BeneficiaryListDomesticModel model = null;
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
	
	public BeneficiaryListDomesticModel getExistingOnlineRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception {
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCorporateIdAndIsBenOnline(accountNo, corporateId, ApplicationConstants.YES);

		BeneficiaryListDomesticModel model = null;
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

	@SuppressWarnings("unchecked")
	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
				
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCorporateId(corporateId);
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_LIST") || map.get(ApplicationConstants.WF_ACTION).equals("DELETE_DOMESTIC_ONLINE_LIST")) {
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

	private BeneficiaryListDomesticModel setMapToModel(BeneficiaryListDomesticModel beneficiaryDomestic, Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);

		beneficiaryDomestic.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryDomestic.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryDomestic.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryDomestic.setBenAliasName((String) map.get("benAliasName"));
		beneficiaryDomestic.setBenAddr1((String) map.get("address1"));
		beneficiaryDomestic.setBenAddr2((String) map.get("address2"));
		beneficiaryDomestic.setBenAddr3((String) map.get("address3"));
		
		beneficiaryDomestic.setLldIsBenResidence((String) map.get("isBenResident"));
//		if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenResidence())){
			CountryModel benResidentCountry = new CountryModel();
			benResidentCountry.setCode((String) map.get("benResidentCountryCode"));
			beneficiaryDomestic.setLldBenResidenceCountry(benResidentCountry);
//		}
		
		beneficiaryDomestic.setLldIsBenCitizen((String) map.get("isBenCitizen"));
//		if(ApplicationConstants.YES.equals(beneficiaryDomestic.getLldIsBenCitizen())){
			CountryModel benCitizenCountry = new CountryModel();
			benCitizenCountry.setCode((String) map.get("benCitizenCountryCode"));
			beneficiaryDomestic.setLldBenCitizenCountry(benCitizenCountry);
//		}
		
		BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
		benType.setCode((String) map.get("beneficiaryTypeCode"));
		beneficiaryDomestic.setBenType(benType);
		
		DomesticBankModel domBank = new DomesticBankModel();
		domBank.setCode((String) map.get("bankCode"));
		beneficiaryDomestic.setBenDomesticBankCode(domBank);
		
		beneficiaryDomestic.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryDomestic.setEmail((String) map.get("email"));

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		beneficiaryDomestic.setCorporate(corporate);

		beneficiaryDomestic.setCorporateUserGroup(corpUser.getCorporateUserGroup());
		beneficiaryDomestic.setIsBenOnline(ApplicationConstants.NO);

		return beneficiaryDomestic;
	}

	private void checkUniqueRecord(String accountNo, String corporateId) throws Exception {
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		if (modelList.size() > 0) {
			BeneficiaryListDomesticModel model = modelList.get(0);
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

			if ("CREATE_DOMESTIC".equals(vo.getAction())) {
				List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
						.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);

				BeneficiaryListDomesticModel beneficiaryDomesticExisting = null;
				if (modelList.size() > 0) {
					beneficiaryDomesticExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryDomesticExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryDomesticExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryDomesticExisting, map);

					saveBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
				} else {
					BeneficiaryListDomesticModel beneficiaryDomestic = new BeneficiaryListDomesticModel();
					setMapToModel(beneficiaryDomestic, map);

					saveBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("UPDATE_DOMESTIC".equals(vo.getAction())) {
				BeneficiaryListDomesticModel beneficiaryDomesticExisting = getExistingRecord(benAccountNo, corporateId,
						true);

				setMapToModel(beneficiaryDomesticExisting, map);

				updateBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
			} else if ("DELETE_DOMESTIC".equals(vo.getAction())) {
				// check existing record exist or not
				BeneficiaryListDomesticModel beneficiaryDomestic = getExistingRecord(benAccountNo, corporateId,
						true);

				deleteBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
			} else if("DELETE_DOMESTIC_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					BeneficiaryListDomesticModel beneficiaryDomestic = getExistingRecord((String) benAccountMap.get("benAccountNo"), corporateId,
							true);

					deleteBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("CREATE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCorporateIdAndIsBenOnline(benAccountNo, corporateId, ApplicationConstants.YES);

				BeneficiaryListDomesticModel beneficiaryDomesticExisting = null;
				if (modelList.size() > 0) {
					beneficiaryDomesticExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryDomesticExisting != null && ApplicationConstants.YES.equals(beneficiaryDomesticExisting.getDeleteFlag())) {
					setMapToModelOnline(beneficiaryDomesticExisting, map);

					saveBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
				} else {
					BeneficiaryListDomesticModel beneficiaryDomestic = new BeneficiaryListDomesticModel();
					setMapToModelOnline(beneficiaryDomestic, map);

					saveBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} else if ("UPDATE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				BeneficiaryListDomesticModel beneficiaryDomesticExisting = getExistingOnlineRecord(benAccountNo, corporateId, true);

				setMapToModelOnline(beneficiaryDomesticExisting, map);

				updateBeneficiaryDomestic(beneficiaryDomesticExisting, vo.getCreatedBy());
			} else if ("DELETE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				// check existing record exist or not
				BeneficiaryListDomesticModel beneficiaryDomestic = getExistingOnlineRecord(benAccountNo, corporateId, true);

				deleteBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
			} else if("DELETE_DOMESTIC_ONLINE_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					BeneficiaryListDomesticModel beneficiaryDomestic = getExistingOnlineRecord((String) benAccountMap.get("benAccountNo"), corporateId, true);

					deleteBeneficiaryDomestic(beneficiaryDomestic, vo.getCreatedBy());
				}
			} 
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	private BeneficiaryListDomesticModel setMapToModelOnline(BeneficiaryListDomesticModel beneficiaryDomestic, Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);

		beneficiaryDomestic.setBenAccountNo((String) map.get("benAccountNo"));
		beneficiaryDomestic.setBenAccountName((String) map.get("benAccountName"));
		beneficiaryDomestic.setBenAccountCurrency((String) map.get("benAccountCurrency"));
		beneficiaryDomestic.setBenAliasName((String) map.get("benAliasName"));
		
		DomesticBankModel domBank = new DomesticBankModel();
		domBank.setCode((String) map.get("bankCode"));
		beneficiaryDomestic.setBenDomesticBankCode(domBank);
		
		beneficiaryDomestic.setIsNotifyBen((String) map.get("isNotify"));
		beneficiaryDomestic.setEmail((String) map.get("email"));

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		beneficiaryDomestic.setCorporate(corporate);

		beneficiaryDomestic.setCorporateUserGroup(corpUser.getCorporateUserGroup());
		
		beneficiaryDomestic.setIsBenOnline(ApplicationConstants.YES);

		return beneficiaryDomestic;
		
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
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
	public void updateBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryDomesticRepo.save(beneficiaryList);
	}

	@Override
	public void deleteBeneficiaryDomestic(BeneficiaryListDomesticModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
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
	public Map<String, Object> searchBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("corporateId", corporateId);
			
			//request by Susi (05 October 2017), bene yg dibuat bisa di lihat oleh semua user di corporate tersebut
//			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
//			map.put("corporateUserGroupId", corporateUser.getCorporateUserGroup().getId());
			Page<BeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map,
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
	public BeneficiaryListDomesticModel findBeneficiary(String benId) throws Exception {
		//TODO implement findByIdAndCorporateUserGroupId jika di masa mendatang beneficiary scope user group
		BeneficiaryListDomesticModel model = beneficiaryDomesticRepo.findOne(benId);

		if (model != null && ApplicationConstants.YES.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}

	@Override
	public void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode,
			String bankCode, String createdBy, boolean isBenOnline) throws Exception {
		
		
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);
		
		//request by Susi, jika sewaktu release udah ada, maka data pakai yg lama
		if(modelList.size() == 0) {
			BeneficiaryListDomesticModel beneficiaryDomestic = new BeneficiaryListDomesticModel();

			beneficiaryDomestic.setBenAccountNo(benAccountNo);
			beneficiaryDomestic.setBenAccountName(benAccountName);
			beneficiaryDomestic.setBenAccountCurrency(benAccountCurrency);
			beneficiaryDomestic.setIsNotifyBen(isNotifyFlag);
			beneficiaryDomestic.setEmail(email);

			CorporateModel corporate = new CorporateModel();
			corporate.setId(corporateId);
			beneficiaryDomestic.setCorporate(corporate);

			CorporateUserGroupModel corporateUserGroup = new CorporateUserGroupModel();
			corporateUserGroup.setId(corporateUserGroupId);
			beneficiaryDomestic.setCorporateUserGroup(corporateUserGroup);
			
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
				
				BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
				benType.setCode(beneficiaryTypeCode);
				beneficiaryDomestic.setBenType(benType);
			}
			
			
			DomesticBankModel domBank = new DomesticBankModel();
			domBank.setCode(bankCode);
			beneficiaryDomestic.setBenDomesticBankCode(domBank);
			
			saveBeneficiaryDomestic(beneficiaryDomestic, createdBy);
		}
		//-------------------------------
	}

	@Override
	public Map<String, Object> searchOnlineBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("corporateId", corporateId);
			map.put("isOnline", ApplicationConstants.YES);
			Page<BeneficiaryListDomesticModel> result = beneficiaryDomesticRepo.search(map, null);

			resultMap.put("result", setModelToMap(result.getContent()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

}
