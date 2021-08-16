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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BeneficiaryListInternationalServiceImpl implements BeneficiaryListInternationalService {

	@Autowired
	private BeneficiaryListInternationalRepository beneficiaryInternationalRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
		

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BeneficiaryListInternationalModel> result = beneficiaryInternationalRepo.search(map,
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

	private List<Map<String, Object>> setModelToMap(List<BeneficiaryListInternationalModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BeneficiaryListInternationalModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(BeneficiaryListInternationalModel model) throws Exception {
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
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_INTERNATIONAL")) {
				checkCustomValidation(map);

				vo.setAction("CREATE_INTERNATIONAL");
				checkUniqueRecord(benAccountNo, corporateId);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_INTERNATIONAL")) {
				checkCustomValidation(map);

				vo.setAction("UPDATE_INTERNATIONAL");

				// check existing record exist or not
				BeneficiaryListInternationalModel beneficiaryInternationalOld = getExistingRecord(benAccountNo, corporateId, true);
				vo.setJsonObjectOld(setModelToMap(beneficiaryInternationalOld));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL")) {
				vo.setAction("DELETE_INTERNATIONAL");

				// check existing record exist or not
				getExistingRecord(benAccountNo, corporateId, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL_LIST")) {
				vo.setAction("DELETE_INTERNATIONAL_LIST");

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
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isInternationalBankValid((String) map.get("bankCode"));
			
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
	public BeneficiaryListInternationalModel getExistingRecord(String accountNo, String corporateId, boolean isThrowError) throws Exception {
		List<BeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		BeneficiaryListInternationalModel model = null;
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
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_INTERNATIONAL_LIST")) {
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

	private BeneficiaryListInternationalModel setMapToModel(BeneficiaryListInternationalModel beneficiaryInternational, Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);
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

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		beneficiaryInternational.setCorporate(corporate);

		beneficiaryInternational.setCorporateUserGroup(corpUser.getCorporateUserGroup());

		return beneficiaryInternational;
	}

	private void checkUniqueRecord(String accountNo, String corporateId) throws Exception {
		/*List<BeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		if (modelList.size() > 0) {
			BeneficiaryListInternationalModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}*/
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String benAccountNo = (String) map.get("benAccountNo");

			if ("CREATE_INTERNATIONAL".equals(vo.getAction())) {
				List<BeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
						.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);

				BeneficiaryListInternationalModel beneficiaryInternationalExisting = null;
				if (modelList.size() > 0) {
					beneficiaryInternationalExisting = modelList.get(0);
				}

				// cek jika record replacement
				if (beneficiaryInternationalExisting != null
						&& ApplicationConstants.YES.equals(beneficiaryInternationalExisting.getDeleteFlag())) {
					setMapToModel(beneficiaryInternationalExisting, map);

					saveBeneficiaryInternational(beneficiaryInternationalExisting, vo.getCreatedBy());
				} else {
					BeneficiaryListInternationalModel beneficiaryInternational = new BeneficiaryListInternationalModel();
					setMapToModel(beneficiaryInternational, map);

					saveBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
				}
			} else if ("UPDATE_INTERNATIONAL".equals(vo.getAction())) {
				BeneficiaryListInternationalModel beneficiaryInternationalExisting = getExistingRecord(benAccountNo, corporateId,
						true);

				setMapToModel(beneficiaryInternationalExisting, map);

				updateBeneficiaryInternational(beneficiaryInternationalExisting, vo.getCreatedBy());
			} else if ("DELETE_INTERNATIONAL".equals(vo.getAction())) {
				// check existing record exist or not
				BeneficiaryListInternationalModel beneficiaryInternational = getExistingRecord(benAccountNo, corporateId,
						true);

				deleteBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
			} else if("DELETE_INTERNATIONAL_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("benAccountList");
				for(Map<String, Object> benAccountMap : benAccountList) {
					BeneficiaryListInternationalModel beneficiaryInternational = getExistingRecord((String) benAccountMap.get("benAccountNo"), corporateId,
							true);

					deleteBeneficiaryInternational(beneficiaryInternational, vo.getCreatedBy());
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
	public void saveBeneficiary(String corporateId, String corporateUserGroupId, String benAccountNo, String benAccountName, String benAccountCurrency,
			String isNotifyFlag, String email, String benAliasName, String address1, String address2, String address3, String isBenResident,
			String benResidentCountryCode, String isBenCitizen, String benCitizenCountryCode,String isBenIdentity,
			String isBenAffiliated, String benCountry,String bankCode, String createdBy) throws Exception {
		
		
		List<BeneficiaryListInternationalModel> modelList = beneficiaryInternationalRepo
				.findByBenAccountNoAndCorporateId(benAccountNo, corporateId);
		
		//request by Susi, jika sewaktu release udah ada, maka data pakai yg lama
		if(modelList.size() == 0) {
			BeneficiaryListInternationalModel beneficiaryInternational = new BeneficiaryListInternationalModel();

			beneficiaryInternational.setBenAccountNo(benAccountNo);
			beneficiaryInternational.setBenAccountName(benAccountName);
			beneficiaryInternational.setBenAccountCurrency(benAccountCurrency);
			beneficiaryInternational.setIsNotifyBen(isNotifyFlag);
			beneficiaryInternational.setEmail(email);

			CorporateModel corporate = new CorporateModel();
			corporate.setId(corporateId);
			beneficiaryInternational.setCorporate(corporate);

			CorporateUserGroupModel corporateUserGroup = new CorporateUserGroupModel();
			corporateUserGroup.setId(corporateUserGroupId);
			beneficiaryInternational.setCorporateUserGroup(corporateUserGroup);
			
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
			
			saveBeneficiaryInternational(beneficiaryInternational, createdBy);
		}
		//-------------------------------
	}
	
	@Override
	public void saveBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String createdBy) throws ApplicationException, BusinessException {
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
	public void updateBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String updatedBy) throws ApplicationException, BusinessException {
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(updatedBy);
		beneficiaryInternationalRepo.save(beneficiaryList);
	}

	@Override
	public void deleteBeneficiaryInternational(BeneficiaryListInternationalModel beneficiaryList, String deletedBy)	throws ApplicationException, BusinessException {
		beneficiaryList.setDeleteFlag(ApplicationConstants.YES);
		beneficiaryList.setUpdatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryList.setUpdatedBy(deletedBy);

		beneficiaryInternationalRepo.save(beneficiaryList);
	}
	
	@Override
	public Map<String, Object> searchBeneficiary(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("corporateId", corporateId);
			
			Page<BeneficiaryListInternationalModel> result = beneficiaryInternationalRepo.search(map,
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
	public BeneficiaryListInternationalModel findBeneficiary(String benId) throws Exception {
		//TODO implement findByIdAndCorporateUserGroupId jika di masa mendatang beneficiary scope user group
		BeneficiaryListInternationalModel model = beneficiaryInternationalRepo.findOne(benId);

		if (model != null && ApplicationConstants.YES.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100100");
		}
		
		return model;
	}

}
