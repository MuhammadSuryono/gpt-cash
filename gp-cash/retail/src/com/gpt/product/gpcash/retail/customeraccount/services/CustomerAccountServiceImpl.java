package com.gpt.product.gpcash.retail.customeraccount.services;

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
import com.gpt.component.maintenance.parametermt.model.AccountProductTypeModel;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customeraccount.repository.CustomerAccountRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerAccountServiceImpl implements CustomerAccountService {

	@Autowired
	private CustomerAccountRepository customerAccountRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;

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

					String customerId = (String) map.get(ApplicationConstants.CUST_ID);
					String accountNo = (String) accountMap.get("accountNo");

					checkUniqueRecord(customerId, accountNo);
				}

			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				List<Map<String, Object>> accountListOld = new ArrayList<>();
				for (Map<String, Object> accountMap : accountList) {
					checkCustomValidation(accountMap);

					String customerId = (String) map.get(ApplicationConstants.CUST_ID);
					String accountNo = (String) accountMap.get("accountNo");
					productRepo.isAccountValid(accountNo);

					CustomerAccountModel customerAccountOld = getExistingRecord(customerId, accountNo, true);

					accountListOld.add(setModelToMap(customerAccountOld, false, true));
				}
				Map<String, Object> accountMap = new HashMap<>();
				accountMap.put("accountList", accountListOld);
				vo.setJsonObjectOld(accountMap);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					String customerId = (String) map.get(ApplicationConstants.CUST_ID);
					String accountNo = (String) accountMap.get("accountNo");
					getExistingRecord(customerId, accountNo, true);
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
	private List<Map<String, Object>> setModelCustomerToMap(List<CustomerModel> list, boolean isGetDetail, boolean isGetFromMasterAccount) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerModel model : list) {
			resultList.add(setModelCustomerToMap(model, isGetDetail, isGetFromMasterAccount));
		}

		return resultList;
	}

	private Map<String, Object> setModelCustomerToMap(CustomerModel model, boolean isGetDetail, boolean isGetFromMasterAccount ) {
		Map<String, Object> map = new HashMap<>();

		map.put("customerId", model.getId());
		map.put("customerName", ValueUtils.getValue(model.getName()));
		map.put("userId", model.getUserId());
		map.put("cifId", model.getHostCifId());
		
		return map;
	}

	private List<Map<String, Object>> setModelToMap(List<CustomerAccountModel> list, boolean isGetDetail, boolean isGetFromMasterAccount) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerAccountModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail, isGetFromMasterAccount));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerAccountModel model, boolean isGetDetail, boolean isGetFromMasterAccount ) {
		Map<String, Object> map = new HashMap<>();

		map.put("id", model.getId());

		AccountModel account = model.getAccount();
		map.put("accountNo", account.getAccountNo());
		map.put("accountName", ValueUtils.getValue(account.getAccountName()));
		map.put("cardNo", ValueUtils.getValue(account.getCardNo()));
		map.put("cifId", ValueUtils.getValue(account.getHostCifId()));
		
		CustomerModel customer = model.getCustomer();
		map.put("customerId", customer.getId());
		map.put("customerName", ValueUtils.getValue(customer.getName()));
		map.put("userId", customer.getUserId());
		
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
		//requested by Susi & Diaz (13 Sept 2017) untuk front line customer account group, master diambil dari customer account
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

	private CustomerAccountModel getExistingRecord(String customerId, String accountNo, boolean isThrowError)	throws Exception {
		CustomerAccountModel model = customerAccountRepo.findByCustomerIdAndAccountNo(customerId, accountNo);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100048", new String[] { accountNo });
		}

		return model;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CUST_ID));
		vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.STR_NAME));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerAccountSC");

		return vo;
	}

	private CustomerAccountModel setMapToModel(Map<String, Object> map, String customerId) throws Exception {
		CustomerAccountModel customerAccount = new CustomerAccountModel();

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		customerAccount.setCustomer(customer);

		AccountModel account = new AccountModel();
		account.setAccountNo((String) map.get("accountNo"));
		account.setAccountName((String) map.get("accountNo"));
		account.setCardNo((String) map.get("cardNo"));
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

		customerAccount.setAccount(account);
		
		customerAccount.setIsDebit((String) map.get("isAllowDebit"));
		customerAccount.setIsCredit((String) map.get("isAllowCredit"));
		customerAccount.setIsInquiry((String) map.get("isAllowInquiry"));
		customerAccount.setInactiveStatus((String) map.get("isInactiveFlag"));

		return customerAccount;
	}

	private void checkUniqueRecord(String customerId, String accountNo) throws Exception {
		if (customerAccountRepo.findByCustomerIdAndAccountNo(customerId, accountNo) != null) {
			throw new BusinessException("GPT-0100047", new String[] { accountNo });
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String customerId = (String) map.get(ApplicationConstants.CUST_ID);

			List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CustomerAccountModel customerAccount = setMapToModel(accountMap, customerId);
					saveCustomerAccount(customerAccount, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CustomerAccountModel customerAccount = setMapToModel(accountMap, customerId);
					updateCustomerAccount(customerAccount, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					CustomerAccountModel customerAccount = setMapToModel(accountMap, customerId);

					// check existing record exist or not
					AccountModel account = customerAccount.getAccount();
					customerAccount = getExistingRecord(customerId, account.getAccountNo(), true);

					deleteCustomerAccount(customerAccount, vo.getCreatedBy());
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
			String userId = (String) map.get(ApplicationConstants.USER_ID);
			String accountNo = (String) map.get("accountNo");
			String name = (String) map.get("customerName");
			
			Page<CustomerModel> result = customerAccountRepo.
					findByUserIdAndAccountNoAndName(Helper.getSearchWildcardValue(userId), 
							Helper.getSearchWildcardValue(accountNo), 
							Helper.getSearchWildcardValue(name), 
							PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelCustomerToMap(result.getContent(), false, true));
			} else {
				resultMap.put("result", setModelCustomerToMap(result.getContent(), true, true));
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
	public Map<String, Object> searchByCustomerIdAndAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String accountNo = "%" + (String) map.get("accountNo") + "%";
			Page<CustomerAccountModel> result = customerAccountRepo.findByCustomerIdAndAccountNoLike(
							(String) map.get(ApplicationConstants.CUST_ID), accountNo,
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
	public Map<String, Object> searchByCustomerId(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerAccountModel> result = customerAccountRepo.findByCustomerId(
							(String) map.get(ApplicationConstants.CUST_ID),
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
	public List<CustomerAccountModel> searchByCustomerId(String customerId) throws ApplicationException, BusinessException {
		try {
			Page<CustomerAccountModel> result = customerAccountRepo.findByCustomerId(customerId, null);
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public List<CustomerAccountModel> findCASAAccountByCustomer(String customerId) throws ApplicationException, BusinessException {
		try {
	    	List<String> casaAccountType = new ArrayList<>();
		    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
		    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
		    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT);
		    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_LOAN);
		    
	    	
			Page<CustomerAccountModel> result = customerAccountRepo.findCASAAccountByCustomer(customerId,
					casaAccountType, null);
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findCASAAccountByCustomerGetMap(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
			Page<CustomerAccountModel> result = customerAccountRepo.findCASAAccountByCustomer(customerId,
					casaAccountType, null);
			
			resultMap.put("accounts", getAccountList(result.getContent()));
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public List<CustomerAccountModel> findCASAAccountByCustomerAndBranch(String customerId, String branchCode) throws ApplicationException, BusinessException {
		try {
			List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
			Page<CustomerAccountModel> result = customerAccountRepo.findCASAAccountByCustomerAndBranchCode(customerId,
					casaAccountType, branchCode, null);
			
			
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public List<CustomerAccountModel> findCASAAccountByCustomerAndAccountType(String customerId, List<String> casaAccountType) throws ApplicationException, BusinessException {
		try {
			Page<CustomerAccountModel> result = customerAccountRepo.findCASAAccountByCustomer(customerId,
					casaAccountType, null);
			
			
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public List<CustomerAccountModel> findCASAAccountByCustomerAndAccountTypeForInquiryOnly(String customerId, List<String> casaAccountType) throws ApplicationException, BusinessException {
		try {
			Page<CustomerAccountModel> result = customerAccountRepo.findCASAAccountByCustomerForInquiryOnly(customerId,
					casaAccountType, null);
			
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findCASAAccountByCustomerAndAccountTypeForInquiryOnlyGetMap(String customerId, List<String> casaAccountType) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			if(casaAccountType == null) {
				casaAccountType = new ArrayList<>();
			    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
			    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
			}
			
			List<CustomerAccountModel> result = findCASAAccountByCustomerAndAccountTypeForInquiryOnly(customerId, casaAccountType);
			
			resultMap.put("accounts", getAccountList(result));
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findByCustomerIdAndIsDebit(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
			Page<CustomerAccountModel> result = customerAccountRepo.findByCustomerIdAndIsDebit(customerId,
					casaAccountType, localCurrencyCode, null);
			
			resultMap.put("accounts", getAccountList(result.getContent()));
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findByCustomerIdAndIsCredit(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<String> casaAccountType = new ArrayList<>();
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    	casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    	
	    	String localCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
	    	
			Page<CustomerAccountModel> result = customerAccountRepo.findByCustomerIdAndIsCredit(customerId,
					casaAccountType, localCurrencyCode, null);
			
			resultMap.put("accounts", getAccountList(result.getContent()));
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private List<Map<String, Object>> getAccountList(List<CustomerAccountModel> modelList){
		List<Map<String, Object>> accountList = new ArrayList<>();
		for(CustomerAccountModel acct : modelList){
			Map<String, Object> account = new HashMap<>();
			
			AccountModel accountModel = acct.getAccount();
			
			account.put(ApplicationConstants.ACCOUNT_DTL_ID, acct.getId());
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
			
			AccountProductTypeModel productType = accountModel.getAccountProductType();
			if (productType != null) {
				account.put("accountProductCode", productType.getCode());
				account.put("accountProductName", productType.getName());
			}
			
			accountList.add(account);
		}
		
		return accountList;
	}

	@Override
	public void saveCustomerAccount(String cifId, String accountNo,
			String accountBranchCode, String accountCurrencyCode, String accountTypeCode,
			String accountName, String accountAlias, String isDebit,
			String isCredit, String isInquiry, String customerId, String createdBy)
			throws ApplicationException, BusinessException {
		try {
			//set account model
			AccountModel account = new AccountModel();
			account.setAccountNo(accountNo);
			account.setAccountName(accountName);
			account.setIsDebit(isDebit);
			account.setIsCredit(isCredit);
			account.setIsInquiry(isInquiry);
			account.setInactiveStatus(ApplicationConstants.NO);
			
			if(ValueUtils.hasValue(cifId)){
				account.setHostCifId(cifId);
			}
			
			BranchModel branch = null;
			if(ValueUtils.hasValue(accountBranchCode)){
				branch = maintenanceRepo.isBranchValid(accountBranchCode);
			} else {
				//get default branch
				branch = maintenanceRepo.getBranchRepo().findOne(maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_BRANCH_CODE).getValue());
			}
			account.setBranch(branch);
			
			CurrencyModel currency = null;
			if(ValueUtils.hasValue(accountCurrencyCode)){
				currency = maintenanceRepo.isCurrencyValid(accountCurrencyCode);
			} else {
				//get default currency
				currency = maintenanceRepo.getCurrencyRepo().findOne(maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue());
			}
			account.setCurrency(currency);
			
			AccountTypeModel accountType = new AccountTypeModel();
			accountType.setCode(accountTypeCode);
			account.setAccountType(accountType);
			
			account.setCreatedBy(createdBy);
			account.setCreatedDate(DateUtils.getCurrentTimestamp());
			//-----------------
			
			//set customer account model
			CustomerAccountModel customerAccount = new CustomerAccountModel();
			
			if(!ValueUtils.hasValue(accountAlias))
				accountAlias = accountName;
			
			customerAccount.setAccountAlias(accountAlias);
			customerAccount.setIsDebit(isDebit);
			customerAccount.setIsCredit(isCredit);
			customerAccount.setIsInquiry(isInquiry);
			customerAccount.setInactiveStatus(ApplicationConstants.NO);
			
			CustomerModel customer = new CustomerModel();
			customer.setId(customerId);
			customerAccount.setCustomer(customer);
			
			customerAccount.setAccount(account);
			//------------------------
		
			saveCustomerAccount(customerAccount, createdBy);
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void saveCustomerAccount(CustomerAccountModel customerAccount, String createdBy)
			throws ApplicationException, BusinessException {

		AccountModel account = customerAccount.getAccount();
		AccountModel accountdb = productRepo.getAccountRepo().findOne(account.getAccountNo());

		if (accountdb == null) {
			// also save to PRO_ACCT
			account.setCreatedBy(createdBy);
			account.setCreatedDate(DateUtils.getCurrentTimestamp());
			productRepo.getAccountRepo().persist(account);
		}

		// set default value
		customerAccount.setCreatedDate(DateUtils.getCurrentTimestamp());
		customerAccount.setCreatedBy(createdBy);

		customerAccountRepo.persist(customerAccount);
	}

	private void updateCustomerAccount(CustomerAccountModel customerAccount, String updatedBy) throws Exception {
		CustomerModel customer = customerAccount.getCustomer();
		AccountModel account = customerAccount.getAccount();
		CustomerAccountModel customerAccountOld = getExistingRecord(customer.getId(),
				account.getAccountNo(), true);

		customerAccountOld.setAccountAlias(customerAccount.getAccountAlias());
		customerAccountOld.setInactiveStatus(customerAccount.getInactiveStatus());
		customerAccountOld.setIsDebit(customerAccount.getIsDebit());
		customerAccountOld.setIsCredit(customerAccount.getIsCredit());
		customerAccountOld.setIsInquiry(customerAccount.getIsInquiry());

		customerAccountOld.setUpdatedDate(DateUtils.getCurrentTimestamp());
		customerAccountOld.setUpdatedBy(updatedBy);
		customerAccountRepo.save(customerAccountOld);
		
	}

	private void deleteCustomerAccount(CustomerAccountModel customerAccount, String deletedBy)
			throws ApplicationException, BusinessException {
		customerAccountRepo.delete(customerAccount);

		// Note : do not delete PRO_ACCT because another customer id might
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
	public int getCountCustomerAccount(String customerId) throws ApplicationException, BusinessException {
		try {
			long totals = (long) customerAccountRepo.getCountByCustomerId(customerId);
			
			return (int) totals;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	}
}
