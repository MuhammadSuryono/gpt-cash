package com.gpt.product.gpcash.corporate.corporateaccount.services;

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
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccount.repository.CorporateAccountRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateAccountServiceImpl implements CorporateAccountService {

	@Autowired
	private CorporateAccountRepository corporateAccountRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
    @Autowired
    private EAIEngine eaiAdapter;	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map); // set model yg mau di simpan ke pending task

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);

				// check unique
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					checkCustomValidation(accountMap);

					String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
					String accountNo = (String) accountMap.get("accountNo");

					checkUniqueRecord(corporateId, accountNo);
				}

			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				List<Map<String, Object>> accountListOld = new ArrayList<>();
				for (Map<String, Object> accountMap : accountList) {
					checkCustomValidation(accountMap);

					String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
					String accountNo = (String) accountMap.get("accountNo");
					productRepo.isAccountValid(accountNo);

					CorporateAccountModel corporateAccountOld = getExistingRecord(corporateId, accountNo, true);

					accountListOld.add(setModelToMap(corporateAccountOld, false, true));
				}
				Map<String, Object> accountMap = new HashMap<>();
				accountMap.put("accountList", accountListOld);
				vo.setJsonObjectOld(accountMap);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
					String accountNo = (String) accountMap.get("accountNo");
					getExistingRecord(corporateId, accountNo, true);
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
			maintenanceRepo.isCurrencyValid((String) map.get("accountCurrencyCode"));
			maintenanceRepo.isAccountTypeValid((String) map.get("accountTypeCode"));

			if(ValueUtils.hasValue(map.get("accountBranchCode"))){
				maintenanceRepo.isBranchValid((String) map.get("accountBranchCode"));
			}
			
			//reject account inactive
			String inactiveFlag = (String) map.get("isInactiveFlag");
			if(ApplicationConstants.YES.equals(inactiveFlag)) {
				throw new BusinessException("GPT-0100157");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private List<Map<String, Object>> setModelToMap(List<CorporateAccountModel> list, boolean isGetDetail, boolean isGetFromMasterAccount) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateAccountModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail, isGetFromMasterAccount));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CorporateAccountModel model, boolean isGetDetail, boolean isGetFromMasterAccount ) {
		Map<String, Object> map = new HashMap<>();

		map.put("id", model.getId());

		AccountModel account = model.getAccount();
		map.put("accountNo", account.getAccountNo());
		map.put("accountName", ValueUtils.getValue(account.getAccountName()));
		
		map.put("cifId", ValueUtils.getValue(account.getHostCifId()));
		
		BranchModel branch = account.getBranch();
		if(branch != null){
			map.put("accountBranchCode", branch.getCode());
			map.put("accountBranchName", branch.getName());
		} else {
			map.put("accountBranchCode", ApplicationConstants.EMPTY_STRING);
			map.put("accountBranchName", ApplicationConstants.EMPTY_STRING);
		}
		
		

		CurrencyModel accountCurrency = account.getCurrency();
		map.put("accountCurrencyCode", ValueUtils.getValue(accountCurrency.getCode()));
		map.put("accountCurrencyName", ValueUtils.getValue(accountCurrency.getName()));
		map.put("isAllowDebit", ValueUtils.getValue(model.getIsDebit()));
		map.put("isAllowCredit", ValueUtils.getValue(model.getIsCredit()));
		map.put("isAllowInquiry", ValueUtils.getValue(model.getIsInquiry()));
		map.put("isInactiveFlag", ValueUtils.getValue(model.getInactiveStatus()));
		
		
		//requested by Susi & Diaz (13 Sept 2017) untuk bank line, master di ambil dari table account
		//requested by Susi & Diaz (13 Sept 2017) untuk front line corporate account group, master diambil dari corporate account
		if(isGetFromMasterAccount) {
			map.put("isAllowDebitMaster", account.getIsDebit());
			map.put("isAllowCreditMaster", account.getIsCredit());
			map.put("isAllowInquiryMaster", account.getIsInquiry());
		} else {
			map.put("isAllowDebitMaster", model.getIsDebit());
			map.put("isAllowCreditMaster", model.getIsCredit());
			map.put("isAllowInquiryMaster", model.getIsInquiry());
		}

		AccountTypeModel accountType = account.getAccountType();
		map.put("accountTypeCode", ValueUtils.getValue(accountType.getCode()));
		map.put("accountTypeName", ValueUtils.getValue(accountType.getName()));

		if (isGetDetail) {
			map.put("accountAlias", ValueUtils.getValue(model.getAccountAlias()));
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

		return map;
	}

	private CorporateAccountModel getExistingRecord(String corporateId, String accountNo, boolean isThrowError)	throws Exception {
		List<CorporateAccountModel> modelList = corporateAccountRepo.findByCorporateIdAndAccountNo(corporateId, accountNo, null).getContent();

		if (modelList.isEmpty()) {
			if (isThrowError)
				throw new BusinessException("GPT-0100048", new String[] { accountNo });
		}

		return (CorporateAccountModel) modelList.get(0);
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CORP_ID));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateAccountSC");

		return vo;
	}

	private CorporateAccountModel setMapToModel(Map<String, Object> map, String corporateId) throws Exception {
		CorporateAccountModel corporateAccount = new CorporateAccountModel();

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		corporateAccount.setCorporate(corporate);

		AccountModel account = new AccountModel();
		account.setAccountNo((String) map.get("accountNo"));
		account.setAccountName((String) map.get("accountName"));
		account.setIsDebit((String) map.get("isAllowDebit"));
		account.setIsCredit((String) map.get("isAllowCredit"));
		account.setIsInquiry((String) map.get("isAllowInquiry"));
		account.setInactiveStatus((String) map.get("isInactiveFlag"));
		
		if(map.get("cifId") != null){
			account.setHostCifId((String) map.get("cifId"));
		}
		
		if(ValueUtils.hasValue(map.get("accountBranchCode"))){
			maintenanceRepo.isBranchValid((String) map.get("accountBranchCode"));
			
			BranchModel branch = new BranchModel();
			branch.setCode((String) map.get("accountBranchCode"));
			account.setBranch(branch);
		}

		CurrencyModel currency = new CurrencyModel();
		currency.setCode((String) map.get("accountCurrencyCode"));
		currency.setName((String) map.get("accountCurrencyName"));
		account.setCurrency(currency);

		AccountTypeModel accountType = new AccountTypeModel();
		accountType.setCode((String) map.get("accountTypeCode"));
		accountType.setName((String) map.get("accountTypeName"));
		account.setAccountType(accountType);

		corporateAccount.setAccount(account);
		
		//set debit limit
		corporateAccount.setDebitLimit(new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_ACCOUNT_DEBIT).getValue()));

		corporateAccount.setIsDebit((String) map.get("isAllowDebit"));
		corporateAccount.setIsCredit((String) map.get("isAllowCredit"));
		corporateAccount.setIsInquiry((String) map.get("isAllowInquiry"));
		corporateAccount.setInactiveStatus((String) map.get("isInactiveFlag"));

		return corporateAccount;
	}

	private void checkUniqueRecord(String corporateId, String accountNo) throws Exception {
		if (corporateAccountRepo.findByCorporateIdAndAccountNo(corporateId, accountNo, null).hasContent()) {
			throw new BusinessException("GPT-0100047", new String[] { accountNo });
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);

			List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CorporateAccountModel corporateAccount = setMapToModel(accountMap, corporateId);
					saveCorporateAccount(corporateAccount, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CorporateAccountModel corporateAccount = setMapToModel(accountMap, corporateId);
					updateCorporateAccount(corporateAccount, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CorporateAccountModel corporateAccount = setMapToModel(accountMap, corporateId);

					// check existing record exist or not
					AccountModel account = corporateAccount.getAccount();
					corporateAccount = getExistingRecord(corporateId, account.getAccountNo(), true);

					deleteCorporateAccount(corporateAccount, vo.getCreatedBy());
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
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return null;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CorporateAccountModel> result = corporateAccountRepo.findByCorporateId(
					(String) map.get(ApplicationConstants.CORP_ID), PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false, true));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true, true));
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
	public Map<String, Object> searchByCorporateIdAndAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String accountNo = "%" + (String) map.get("accountNo") + "%";
			Page<CorporateAccountModel> result = corporateAccountRepo.findByCorporateIdAndAccountNoLike(
							(String) map.get(ApplicationConstants.LOGIN_CORP_ID), accountNo,
							PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent(), false, false));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public List<CorporateAccountModel> searchByCorporateId(String corporateId) throws ApplicationException, BusinessException {
		try {
			Page<CorporateAccountModel> result = corporateAccountRepo.findByCorporateId(corporateId, null);
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public List<CorporateAccountModel> findCASAAccountByCorporate(String corporateId) throws ApplicationException, BusinessException {
		try {
			List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
			Page<CorporateAccountModel> result = corporateAccountRepo.findCASAAccountByCorporate(corporateId,
					casaAccountType, null);
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private void saveCorporateAccount(CorporateAccountModel corporateAccount, String createdBy)
			throws ApplicationException, BusinessException {

		AccountModel account = corporateAccount.getAccount();
		AccountModel accountdb = productRepo.getAccountRepo().findOne(account.getAccountNo());

		if (accountdb == null) {
			// also save to PRO_ACCT
			account.setCreatedBy(createdBy);
			account.setCreatedDate(DateUtils.getCurrentTimestamp());
			productRepo.getAccountRepo().persist(account);
		}

		// set default value
		corporateAccount.setCreatedDate(DateUtils.getCurrentTimestamp());
		corporateAccount.setCreatedBy(createdBy);

		corporateAccountRepo.persist(corporateAccount);
	}

	private void updateCorporateAccount(CorporateAccountModel corporateAccount, String updatedBy) throws Exception {
		CorporateModel corporate = corporateAccount.getCorporate();
		AccountModel account = corporateAccount.getAccount();
		CorporateAccountModel corporateAccountOld = getExistingRecord(corporate.getId(),
				account.getAccountNo(), true);

		corporateAccountOld.setAccountAlias(corporateAccount.getAccountAlias());
		corporateAccountOld.setDebitLimit(corporateAccount.getDebitLimit());
		corporateAccountOld.setInactiveStatus(corporateAccount.getInactiveStatus());
		corporateAccountOld.setIsDebit(corporateAccount.getIsDebit());
		corporateAccountOld.setIsCredit(corporateAccount.getIsCredit());
		corporateAccountOld.setIsInquiry(corporateAccount.getIsInquiry());

		corporateAccountOld.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateAccountOld.setUpdatedBy(updatedBy);
		corporateAccountRepo.save(corporateAccountOld);
		
		//update isDebit, isCredit dan isInquiry ke corporate account group detail berdasarkan corp_acct_no id 
		//(request by Susi 06 Sept 2017 untuk update 1 baris)
		corporateAccountGroupRepo.updateDetailByCorporateAccountId(corporateAccountOld.getId(), 
				corporateAccount.getIsInquiry(), corporateAccount.getIsCredit(), corporateAccount.getIsDebit());
		
	}

	private void deleteCorporateAccount(CorporateAccountModel corporateAccount, String deletedBy)
			throws ApplicationException, BusinessException {
		// also delete corporate account group detail where corporateAccountNo
		corporateAccountGroupRepo.deleteDetailByCorporateAccountId(corporateAccount.getId());

		corporateAccountRepo.delete(corporateAccount);

		// Note : do not delete PRO_ACCT because another corporate id might
		// still use this account
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> searchOnlineByCIF(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("cifId", map.get("cifId"));
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.RETRIEVE_ACCOUNTS_BY_CIF, inputs);
			
			List<Map<String,Object>> accounts = new ArrayList<>();
			List<Map<String,Object>> accountList = (List<Map<String,Object>>)outputs.get("accountList");
			if (ValueUtils.hasValue(accountList)) {
				
				for (Map<String,Object> temp : accountList) {
					Map<String, Object> account = new HashMap<>();
					account.put("accountNo", temp.get("accountNo"));
					account.put("accountName", temp.get("accountName"));
					
					account.put("accountTypeCode", temp.get("accountTypeCode"));
					account.put("accountTypeName", maintenanceRepo.isAccountTypeValid((String)temp.get("accountTypeCode")).getName());
					account.put("accountCurrencyCode", temp.get("accountCurrencyCode"));
					account.put("accountCurrencyName", maintenanceRepo.isCurrencyValid((String)temp.get("accountCurrencyCode")).getName());
					
					String accountBranchCode = (String)temp.get("accountBranchCode");
					BranchModel accountBranch = maintenanceRepo.getBranchRepo().findOne(accountBranchCode);
					//jika tidak ada di maintenance maka ambil default
					if(accountBranch == null) {
						accountBranch = maintenanceRepo.getBranchRepo().findOne(maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_BRANCH_CODE).getValue());
						account.put("accountBranchCode", accountBranchCode);
						account.put("accountBranchName", accountBranch.getName());
					}
					
					account.put("accountBranchCode", accountBranchCode);
					account.put("accountBranchName", accountBranch.getName());
					
					account.put("accountStatus", temp.get("accountStatus"));
					account.put("isAllowDebit", temp.get("isAllowDebit"));
					account.put("isAllowCredit", temp.get("isAllowCredit"));
					account.put("isAllowInquiry", temp.get("isAllowInquiry"));
					
					//requested untuk keperluan ui, karena ui udah baca key master untuk bisa edit / tidak agar sama dengan edit
					account.put("isAllowDebitMaster", temp.get("isAllowDebit"));
					account.put("isAllowCreditMaster", temp.get("isAllowCredit"));
					account.put("isAllowInquiryMaster", temp.get("isAllowInquiry"));
					
					accounts.add(account);
				}
				
			}
			
			Map<String, Object> result = new HashMap<>(2,1);
			result.put("cifId", map.get("cifId"));
			result.put("accountList", accounts);
			
			return result;
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchOnlineByAccountNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", map.get("accountNo"));
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
			
			Map<String, Object> result = new HashMap<>();
			result.put("cifId", outputs.get("cifId"));
			result.put("accountNo", outputs.get("accountNo"));
			result.put("accountName", outputs.get("accountName"));
			result.put("accountTypeCode", outputs.get("accountTypeCode"));
			result.put("accountTypeName", maintenanceRepo.isAccountTypeValid((String)outputs.get("accountTypeCode")).getName());
			result.put("accountCurrencyCode", outputs.get("accountCurrencyCode"));
			result.put("accountCurrencyName", maintenanceRepo.isCurrencyValid((String)outputs.get("accountCurrencyCode")).getName());
			
			String accountBranchCode = (String)outputs.get("accountBranchCode");
			BranchModel accountBranch = maintenanceRepo.getBranchRepo().findOne(accountBranchCode);
			//jika tidak ada di maintenance maka ambil default
			if(accountBranch == null) {
				accountBranch = maintenanceRepo.getBranchRepo().findOne(maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_BRANCH_CODE).getValue());
				result.put("accountBranchCode", accountBranchCode);
				result.put("accountBranchName", accountBranch.getName());
			}
			
			result.put("accountStatus", outputs.get("accountStatus"));
			result.put("isAllowDebit", outputs.get("isAllowDebit"));
			result.put("isAllowCredit", outputs.get("isAllowCredit"));
			result.put("isAllowInquiry", outputs.get("isAllowInquiry"));	
			
			//requested untuk keperluan ui, karena ui udah baca key master untuk bisa edit / tidak agar sama dengan edit
			result.put("isAllowDebitMaster", outputs.get("isAllowDebit"));
			result.put("isAllowCreditMaster", outputs.get("isAllowCredit"));
			result.put("isAllowInquiryMaster", outputs.get("isAllowInquiry"));
			
			return result;
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public List<CorporateAccountModel> findCAAccountForVAByCorporate(String corporateId) throws ApplicationException, BusinessException {
		try {
			
			return corporateAccountRepo.findCAAccountForVAByCorporate(corporateId);
				
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
		
}
