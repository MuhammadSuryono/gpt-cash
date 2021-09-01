package com.gpt.product.gpcash.corporate.corporateaccountgroup.services;

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
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateAccountGroupServiceImpl implements CorporateAccountGroupService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
    private EAIEngine eaiAdapter;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CorporateAccountGroupModel> result = corporateAccountGroupRepo.search(map,
					PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public List<Map<String, Object>> searchByCorporateId(String corporateId, boolean isGetDetail) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> searchMap = new HashMap<>();
			searchMap.put(ApplicationConstants.CORP_ID, corporateId);
			Page<CorporateAccountGroupModel> result = corporateAccountGroupRepo.search(searchMap,null);

			return setModelToMap(result.getContent(), isGetDetail);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	}
	
	@Override
	public int getCountCorporateIdAndGroupId(String corporateId, String corporateAccountGroupId) throws ApplicationException, BusinessException {
		try {
			long totals = (long) corporateAccountGroupRepo.getCountByCorporateIdAndAccountGroupId
					(corporateId, corporateAccountGroupId);
			
			return (int) totals;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	}

	private List<Map<String, Object>> setModelToMap(List<CorporateAccountGroupModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateAccountGroupModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CorporateAccountGroupModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));

		if (isGetDetail) {
			if (model.getCorporateAccountGroupDetail() != null) {
				List<Map<String, String>> listDetail = new ArrayList<>();
				for (CorporateAccountGroupDetailModel detailModel : model.getCorporateAccountGroupDetail()) {
					Map<String, String> detail = new HashMap<>();

					CorporateAccountModel corporateAccount = detailModel.getCorporateAccount();
					AccountModel account = corporateAccount.getAccount();

					CurrencyModel currency = account.getCurrency();

					detail.put("accountNo", account.getAccountNo());
					detail.put("accountName", account.getAccountName());
					detail.put("accountCurrencyCode", currency.getCode());
					detail.put("accountCurrencyName", currency.getName());
					detail.put("isAllowDebit", detailModel.getIsDebit());
					detail.put("isAllowCredit", detailModel.getIsCredit());
					detail.put("isAllowInquiry", detailModel.getIsInquiry());
					
					
					//requested by Susi & Diaz (13 Sept 2017) agar master di corporate account group di ambil dari corporate account
					detail.put("isAllowDebitMaster", corporateAccount.getIsDebit());
					detail.put("isAllowCreditMaster", corporateAccount.getIsCredit());
					detail.put("isAllowInquiryMaster", corporateAccount.getIsInquiry());

					listDetail.add(detail);
				}
				map.put("accountList", listDetail);
			}

			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			CorporateAdminPendingTaskVO vo = setCorporateAdminPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get(ApplicationConstants.STR_CODE));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				CorporateAccountGroupModel corporateAccountGroupOld = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(corporateAccountGroupOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				checkCustomValidation(map);
				
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				
				String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
				String accountGroupCode = (String) map.get(ApplicationConstants.STR_CODE);
				
				//validasi jika masih terdapat user group yg memakai account group ini
				CorporateAccountGroupModel corporateAccountGroup = getExistingRecord(corporateId,
						accountGroupCode, true);
				
				List<CorporateUserGroupModel> corporateUserGroupList = corporateUtilsRepo.getCorporateUserGroupRepo().
						findByCorporateIdAndAccountGroupCode(corporateId, corporateAccountGroup.getCode());
				
				if(corporateUserGroupList.size() > 0) {
					throw new BusinessException("GPT-0100155");
				}
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_ACCOUNT")) {
				vo.setAction("DELETE_ACCOUNT");

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);
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

	@SuppressWarnings("unchecked")
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			// check if account is registered in CorporateAccount for that
			// corporateId
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

			List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");

			if(accountList != null) {
				for (Map<String, Object> accountGroupListMap : accountList) {
					String accountNo = (String) accountGroupListMap.get("accountNo");

					corporateUtilsRepo.isCorporateAccountValid(corporateId, accountNo);
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CorporateAccountGroupModel getExistingRecord(String corporateId, String code, boolean isThrowError)	throws Exception {
		CorporateAccountGroupModel model = corporateAccountGroupRepo.findByCorporateIdAndCode(corporateId, code);

		if (model==null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
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

	private CorporateAdminPendingTaskVO setCorporateAdminPendingTaskVO(Map<String, Object> map) {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.STR_CODE)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateAccountGroupSC");
		vo.setCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));

		return vo;
	}

	@SuppressWarnings("unchecked")
	private CorporateAccountGroupModel setMapToModel(CorporateAccountGroupModel corporateAccountGroup,
			Map<String, Object> map, boolean isNew, String createdBy) throws Exception {
		corporateAccountGroup.setCode((String) map.get(ApplicationConstants.STR_CODE));
		corporateAccountGroup.setName((String) map.get(ApplicationConstants.STR_NAME));

		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		corporateAccountGroup.setCorporate(corporate);

		List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");

		corporateAccountGroup.setCorporateAccountGroupDetail(
				getCorporateAccountGroupDetailList(accountList, isNew, corporateAccountGroup, corporateId, createdBy));

		return corporateAccountGroup;
	}
	
	private List<CorporateAccountGroupDetailModel> getCorporateAccountGroupDetailList(List<Map<String, Object>> accountList, boolean isNew,
			CorporateAccountGroupModel corporateAccountGroup, String corporateId, String createdBy) throws Exception{
		List<CorporateAccountGroupDetailModel> corporateAccountGroupDetailList = new ArrayList<>();
		
		List<String> detailListOld = new ArrayList<>();
		if(!isNew) {
			//compare dengan data lama, bisa saja account ada yg di remove
			detailListOld = corporateAccountGroupRepo
					.findDetailByAccountGroupId(corporateId, corporateAccountGroup.getId());
			
			if(logger.isDebugEnabled()) {
				logger.debug("detailListOld : " + detailListOld);
			}
		}
		
		for (Map<String, Object> accountGroupListMap : accountList) {
			if(logger.isDebugEnabled()) {
				logger.debug("process accountGroupListMap : " + accountGroupListMap);
			}
			
			String accountNo = (String) accountGroupListMap.get("accountNo");
			String isAllowDebit = (String) accountGroupListMap.get("isAllowDebit");
			String isAllowCredit = (String) accountGroupListMap.get("isAllowCredit");
			String isAllowInquiry = (String) accountGroupListMap.get("isAllowInquiry");

			CorporateAccountGroupDetailModel detail = new CorporateAccountGroupDetailModel();

			if (isNew) {
				// if new for sure only add new
				detail = createNewDataCorporateAccountGroupDetail(corporateAccountGroup, accountNo, isAllowDebit,
						isAllowCredit, isAllowInquiry, createdBy);
			} else {

				// check account exist or not
				List<CorporateAccountGroupDetailModel> detailList = corporateAccountGroupRepo
						.findDetailByAccountNoAndAccountGroupId(corporateId, accountNo, corporateAccountGroup.getId());

				// if exist then update data
				if (detailList.size() > 0) {
					detail = detailList.get(0);
					detail.setIsDebit(isAllowDebit);
					detail.setIsCredit(isAllowCredit);
					detail.setIsInquiry(isAllowInquiry);
					detail.setUpdatedBy(createdBy);
					detail.setUpdatedDate(DateUtils.getCurrentTimestamp());
					
					//cek ke detailListOld jika ada maka remove
					if(detailListOld.contains(detail.getId())) {
						detailListOld.remove(detail.getId());
					}
					
				} else { // else add new data
					detail = createNewDataCorporateAccountGroupDetail(corporateAccountGroup, accountNo, isAllowDebit,
							isAllowCredit, isAllowInquiry, createdBy);
				}
			}

			corporateAccountGroupDetailList.add(detail);
		}
		
		if(!isNew) {
			if(logger.isDebugEnabled()) {
				logger.debug("remove corporate account group detail id from account group : " + detailListOld);
			}
			if(detailListOld.size() > 0) {
				//jika ada data di detailListOld maka perlu di delete, karena di remove oleh user
				corporateAccountGroupRepo.deleteDetailById(detailListOld);
			}
			
		}
		
		return corporateAccountGroupDetailList;
	}

	private CorporateAccountGroupDetailModel createNewDataCorporateAccountGroupDetail(
			CorporateAccountGroupModel corporateAccountGroup, String accountNo, String isAllowDebit,
			String isAllowCredit, String isAllowInquiry, String createdBy) throws Exception {
		CorporateAccountGroupDetailModel detail = new CorporateAccountGroupDetailModel();

		CorporateModel corporate = corporateAccountGroup.getCorporate();
		detail.setCorporateAccount(corporateUtilsRepo.getCorporateAccountRepo()
				.findByCorporateIdAndAccountNo(corporate.getId(), accountNo, null).getContent().get(0));

		detail.setCorporateAccountGroup(corporateAccountGroup);

		detail.setIsDebit(isAllowDebit);
		detail.setIsCredit(isAllowCredit);
		detail.setIsInquiry(isAllowInquiry);
		detail.setCreatedBy(createdBy);
		detail.setCreatedDate(DateUtils.getCurrentTimestamp());

		return detail;
	}

	private void checkUniqueRecord(String corporateId, String code) throws Exception {
		CorporateAccountGroupModel model = corporateAccountGroupRepo.findByCorporateIdAndCode(corporateId, code);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag()))
			throw new BusinessException("GPT-0100004");
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CorporateAccountGroupModel corporateAccountGroupExisting = corporateAccountGroupRepo.findByCorporateIdAndCode(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE));

				// cek jika record replacement
				if (corporateAccountGroupExisting != null
						&& ApplicationConstants.YES.equals(corporateAccountGroupExisting.getDeleteFlag())) {
					setMapToModel(corporateAccountGroupExisting, map, true, vo.getCreatedBy());

					saveCorporateAccountGroup(corporateAccountGroupExisting, vo.getCreatedBy());
				} else {
					CorporateAccountGroupModel corporateAccountGroup = new CorporateAccountGroupModel();
					setMapToModel(corporateAccountGroup, map, true, vo.getCreatedBy());

					saveCorporateAccountGroup(corporateAccountGroup, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CorporateAccountGroupModel corporateAccountGroupExisting = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);

				setMapToModel(corporateAccountGroupExisting, map, false, vo.getCreatedBy());

				updateCorporateAccountGroup(corporateAccountGroupExisting, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				CorporateAccountGroupModel corporateAccountGroup = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);

				deleteCorporateAccountGroup(corporateAccountGroup, vo.getCreatedBy(), vo.getCorporateId());
			} else if ("DELETE_ACCOUNT".equals(vo.getAction())) {
				// check existing record exist or not
				CorporateAccountGroupModel corporateAccountGroup = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);

				deleteCorporateAccountGroupDetailByAccountList(corporateAccountGroup,
						(ArrayList<Map<String, Object>>) map.get("accountList"));

			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = corporateAccountGroup.getDeleteFlag() == null;
		corporateAccountGroup.setDeleteFlag(ApplicationConstants.NO);
		corporateAccountGroup.setCreatedDate(DateUtils.getCurrentTimestamp());
		corporateAccountGroup.setCreatedBy(createdBy);
		corporateAccountGroup.setUpdatedDate(null);
		corporateAccountGroup.setUpdatedBy(null);

		if (isNew)
			corporateAccountGroupRepo.persist(corporateAccountGroup);
		else
			corporateAccountGroupRepo.save(corporateAccountGroup);
	}

	@Override
	public void updateCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String updatedBy)
			throws ApplicationException, BusinessException {
		corporateAccountGroup.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateAccountGroup.setUpdatedBy(updatedBy);
		corporateAccountGroupRepo.save(corporateAccountGroup);
	}

	@Override
	public void deleteCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup, String deletedBy
			, String corporateId) throws ApplicationException, BusinessException {
		try {
			corporateAccountGroup.setDeleteFlag(ApplicationConstants.YES);
			corporateAccountGroup.setUpdatedDate(DateUtils.getCurrentTimestamp());
			corporateAccountGroup.setUpdatedBy(deletedBy);
	
			corporateAccountGroupRepo.deleteDetailByAccountGroupId(corporateAccountGroup.getId());
	
			corporateAccountGroupRepo.save(corporateAccountGroup);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	public void deleteCorporateAccountGroupDetailByAccountList(CorporateAccountGroupModel corporateAccountGroup,
			List<Map<String, Object>> accountList) throws Exception {

		List<String> accountListStr = new ArrayList<>();
		for (Map<String, Object> accountMap : accountList) {
			accountListStr.add((String) accountMap.get("accountNo"));
		}

		if (accountListStr.size() > 0) {
			List<CorporateAccountGroupDetailModel> accountGroupDetailList = corporateAccountGroupRepo.findDetailByAccountNoList(corporateAccountGroup.getCorporate().getId(), accountListStr);
			
			for(CorporateAccountGroupDetailModel detail : accountGroupDetailList){
				corporateAccountGroupRepo.deleteDetailById(detail.getId());
			}
			
		}

	}
	
	@Override
	public AccountModel searchAccountByAccountNoForInquiryOnly(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountNoAndAccountGroupIdAndIsInquiry(corporateId, accountNo, accountGroup.getId());

			if (modelList.size() == 0) {
				if(isThrowError)
					throw new BusinessException("GPT-0100077");
			}
	    	
	    	return modelList.get(0).getCorporateAccount().getAccount();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailByBranchForInquiryOnly(String corporateId, String userCode, String branchCode) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	return corporateAccountGroupRepo.findDetailByBranchCodeAndAccountGroupIdAndIsInquiry(corporateId, branchCode, accountGroup.getId());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(String corporateId, String userCode, List<String> accountTypeList) throws ApplicationException, BusinessException {
		try {
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();

	    	List<String> casaAccountType = new ArrayList<>();
	    	
	    	if(accountTypeList == null) {
		    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
		    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
		    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_LOAN);
		    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT);
	    	} else {
	    		casaAccountType = accountTypeList;
	    	}
	    	
	    	return corporateAccountGroupRepo.findDetailByAccountTypeAndAccountGroupIdAndIsInquiry(corporateId, casaAccountType, accountGroup.getId());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForInquiryOnly(String corporateId, String userCode) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsInquiry(corporateId, accountGroup.getId());			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForInquiryOnlyGetMap(String corporateId, String userCode, List<String> accountTypeList) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(corporateId, userCode, accountTypeList);
		
		resultMap.put("accounts", getAccountList(modelList, false));
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForInquiryOnlyGetMapWithProductName(String corporateId, String userCode, List<String> accountTypeList) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(corporateId, userCode, accountTypeList);
		
		resultMap.put("accounts", getAccountList(modelList, true));
		
		return resultMap;
	}
	
	private List<Map<String, Object>> getAccountList(List<CorporateAccountGroupDetailModel> modelList, boolean isGetProductDescription){
		List<Map<String, Object>> accountList = new ArrayList<>();
		for(CorporateAccountGroupDetailModel accountGroupDetail : modelList){
			Map<String, Object> account = new HashMap<>();
			
			AccountModel accountModel = accountGroupDetail.getCorporateAccount().getAccount();
			
			account.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, accountGroupDetail.getId());
			account.put("accountNo", accountModel.getAccountNo());
			account.put("accountName", accountModel.getAccountName());

			AccountTypeModel accountType = accountModel.getAccountType();
			account.put("accountTypeCode", accountType.getCode());
			account.put("accountTypeName", accountType.getName());

			CurrencyModel accountCurrency = accountModel.getCurrency();
			account.put("accountCurrencyCode", accountCurrency.getCode());
			account.put("accountCurrencyName", accountCurrency.getName());

			BranchModel branch = accountModel.getBranch();

			if (branch != null) {
				account.put("accountBranchCode", branch.getCode());
				account.put("accountBranchName", branch.getName());
			}
			
			if(isGetProductDescription) {
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("accountNo", accountModel.getAccountNo());
				try {
					Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
					account.put("productDescription", outputs.get("productDescription"));
				} catch (Exception e) {
					logger.error("Error while doAccountInquiry");
				}
			}
			
			accountList.add(account);
		}
		
		return accountList;
	}
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForDebitOnly(String corporateId, String userCode) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
	    	//TODO remove local currency if implement forex transaction
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsDebit(corporateId, accountGroup.getId(), casaAccountType, localCurrencyCode);
	    	
	    	//return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsDebitMultiCurrency(corporateId, accountGroup.getId(), casaAccountType);
	    	
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForDebitOnlyGetMap(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailForDebitOnly(corporateId, userCode);
		
		resultMap.put("accounts", getAccountList(modelList, false));
		
		return resultMap;
	}
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForDebitOnlyMultiCurrency(String corporateId, String userCode) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	/*String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
	    	//TODO remove local currency if implement forex transaction
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsDebit(corporateId, accountGroup.getId(), casaAccountType, localCurrencyCode);*/
	    	
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsDebitMultiCurrency(corporateId, accountGroup.getId(), casaAccountType);
	    	
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForDebitOnlyMultiCurrencyGetMap(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailForDebitOnlyMultiCurrency(corporateId, userCode);
		
		resultMap.put("accounts", getAccountList(modelList, false));
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForDebitOnlyGetMapForBank(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailForDebitOnlyForBank(corporateId);
		
		resultMap.put("accounts", getAccountList(modelList, false));
		
		return resultMap;
	}
	
	private List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForDebitOnlyForBank(String corporateId) throws ApplicationException, BusinessException {
		try {
	    	List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
	    	//TODO remove local currency if implement forex transaction
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsDebitForBank(corporateId,
	    			casaAccountType, localCurrencyCode);			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	
	@Override
	public List<CorporateAccountGroupDetailModel> searchCorporateAccountGroupDetailForCreditOnly(String corporateId, String userCode) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
	    	//TODO remove local currency if implement forex transaction
	    	//OK saya remove localcurrencynya, 31 Agustus 2021
	    	return corporateAccountGroupRepo.findDetailByAccountGroupIdAndIsCredit(corporateId, accountGroup.getId(),
	    			casaAccountType);			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailForCreditOnlyGetMap(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailForCreditOnly(corporateId, userCode);
		
		resultMap.put("accounts", getAccountList(modelList, false));
		
		return resultMap;
	}
	
	@Override
	public CorporateAccountGroupDetailModel searchCorporateAccountByAccountNo(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountNoAndAccountGroupIdAndIsInquiry(corporateId, accountNo, accountGroup.getId());

			if (modelList == null || modelList.isEmpty()) {
				if(isThrowError)
					throw new BusinessException("GPT-0100077");
			}
	    	
	    	return modelList.get(0);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public CorporateAccountGroupDetailModel searchCorporateAccountByAccountNoForCredit(String corporateId, String userCode, String accountNo, boolean isThrowError) throws ApplicationException, BusinessException {
		try {
	    	CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
	    	
	    	CorporateAccountGroupModel accountGroup = corporateUser.getCorporateUserGroup().getCorporateAccountGroup();
	    	
	    	List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
	    	List<CorporateAccountGroupDetailModel> modelList = corporateAccountGroupRepo.findDetailByAccountGroupIdCorpAccAndIsCredit(corporateId, accountNo, accountGroup.getId(), casaAccountType, localCurrencyCode);

			if (modelList == null || modelList.isEmpty()) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100077");
				} else {
					return null;
				}
			}
	    	
	    	return modelList.get(0);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
    }
	
	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailVirtualAccountForInquiryOnly(String corporateId, String userCode, List<String> accountTypeList, boolean isGetProductDescription) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(corporateId, userCode, accountTypeList);
		
		resultMap.put("accounts", getAccountListVirtualAccountOnly(modelList, isGetProductDescription));
		
		return resultMap;
	}
	
	private List<Map<String, Object>> getAccountListVirtualAccountOnly(List<CorporateAccountGroupDetailModel> modelList, boolean isGetProductDescription){
		List<Map<String, Object>> accountList = new ArrayList<>();
		for(CorporateAccountGroupDetailModel accountGroupDetail : modelList){
			
			AccountModel accountModel = accountGroupDetail.getCorporateAccount().getAccount();
			
			if (accountModel.getAccountNo().length() > ApplicationConstants.MAX_LENGTH_ACCOUNT_REG) {
				Map<String, Object> account = new HashMap<>();
				
				account.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, accountGroupDetail.getId());
				account.put("accountNo", accountModel.getAccountNo());
				account.put("accountName", accountModel.getAccountName());
				
				AccountTypeModel accountType = accountModel.getAccountType();
				account.put("accountTypeCode", accountType.getCode());
				account.put("accountTypeName", accountType.getName());
				
				CurrencyModel accountCurrency = accountModel.getCurrency();
				account.put("accountCurrencyCode", accountCurrency.getCode());
				account.put("accountCurrencyName", accountCurrency.getName());
				
				BranchModel branch = accountModel.getBranch();
				
				if (branch != null) {
					account.put("accountBranchCode", branch.getCode());
					account.put("accountBranchName", branch.getName());
				}
				
				if(isGetProductDescription) {
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", accountModel.getAccountNo());
					try {
						Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
						account.put("productDescription", outputs.get("productDescription"));
					} catch (Exception e) {
						logger.error("Error while doAccountInquiry");
					}
				}
				
				accountList.add(account);
			}
		}
		
		return accountList;
	}

	@Override
	public Map<String, Object> searchCorporateAccountGroupDetailNonVirtualAccountForInquiryOnlyGetMap(String corporateId, String userCode, List<String> accountTypeList, boolean isGetProductDescription) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		List<CorporateAccountGroupDetailModel> modelList = searchCorporateAccountGroupDetailByAccountTypeForInquiryOnly(corporateId, userCode, accountTypeList);
		
		resultMap.put("accounts", getAccountListNonVirtualAccount(modelList, isGetProductDescription));
		
		return resultMap;
	}
	
	private List<Map<String, Object>> getAccountListNonVirtualAccount(List<CorporateAccountGroupDetailModel> modelList, boolean isGetProductDescription){
		List<Map<String, Object>> accountList = new ArrayList<>();
		for(CorporateAccountGroupDetailModel accountGroupDetail : modelList){
			Map<String, Object> account = new HashMap<>();
			
			AccountModel accountModel = accountGroupDetail.getCorporateAccount().getAccount();
			if (accountModel.getAccountNo().length() <= ApplicationConstants.MAX_LENGTH_ACCOUNT_REG) {
				
				account.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, accountGroupDetail.getId());
				account.put("accountNo", accountModel.getAccountNo());
				account.put("accountName", accountModel.getAccountName());
				
				AccountTypeModel accountType = accountModel.getAccountType();
				account.put("accountTypeCode", accountType.getCode());
				account.put("accountTypeName", accountType.getName());
				
				CurrencyModel accountCurrency = accountModel.getCurrency();
				account.put("accountCurrencyCode", accountCurrency.getCode());
				account.put("accountCurrencyName", accountCurrency.getName());
				
				BranchModel branch = accountModel.getBranch();
				
				if (branch != null) {
					account.put("accountBranchCode", branch.getCode());
					account.put("accountBranchName", branch.getName());
				}
				
				if(isGetProductDescription) {
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", accountModel.getAccountNo());
					try {
						Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
						account.put("productDescription", outputs.get("productDescription"));
					} catch (Exception e) {
						logger.error("Error while doAccountInquiry");
					}
				}
				
				accountList.add(account);
			}
		}
		
		return accountList;
	}
}
