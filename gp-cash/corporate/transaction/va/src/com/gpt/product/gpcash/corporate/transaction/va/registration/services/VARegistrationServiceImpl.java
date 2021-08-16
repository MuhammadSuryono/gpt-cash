package com.gpt.product.gpcash.corporate.transaction.va.registration.services;

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
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.account.repository.AccountRepository;
import com.gpt.product.gpcash.corporate.corporate.VAStatus;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccount.services.CorporateAccountService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.constants.VARegistrationConstants;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationDetailModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.repository.VARegistrationRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class VARegistrationServiceImpl implements VARegistrationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private AccountRepository accountRepo;
	
	@Autowired
	private CorporateAccountService corporateAccountService;
	
	@Autowired
	private PendingTaskService pendingTaskService;

    @Autowired
    private EAIEngine eaiAdapter;
    
    @Autowired
	private CorporateRepository corporateRepo;
    
    @Autowired
    private VARegistrationRepository vaRegistrationRepo;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;
	
	@Override
	public Map<String, Object> searchVAListByCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<VARegistrationModel> result = vaRegistrationRepo.search(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<VARegistrationModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (VARegistrationModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(VARegistrationModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		AccountModel account = model.getMainAccountNo();
		
		map.put("accountNo", account.getAccountNo());
		map.put("accountName", account.getAccountName());
		map.put("accountCurrencyCode", account.getCurrency().getCode());
		map.put("productCode", model.getProductCode());
		map.put("productName", model.getProductName());

		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		map.put("id", model.getId());

		return map;
	}
	
	@Override
	public Map<String, Object> searchCorporateAccount(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<CorporateAccountModel> corporateAccounts = corporateAccountService.findCAAccountForVAByCorporate(corporateId);
			
			List<Map<String, Object>> accounts = new ArrayList<>();
			
			for(CorporateAccountModel corporateAccount : corporateAccounts) {
				Map<String, Object> account = new HashMap<>();
				
				AccountModel accountModel = corporateAccount.getAccount();
				account.put("accountNo", accountModel.getAccountNo());
				account.put("accountName", accountModel.getAccountName());

				AccountTypeModel accountType = accountModel.getAccountType();
				account.put("accountTypeCode", accountType.getCode());
				account.put("accountTypeName", accountType.getName());

				CurrencyModel accountCurrency = accountModel.getCurrency();
				account.put("accountCurrencyCode", accountCurrency.getCode());
				account.put("accountCurrencyName", accountCurrency.getName());
				
				accounts.add(account);
			}
			
			resultMap.put("accounts", accounts);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map); // set model yg mau di simpan ke pending task

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if(map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE_STATUS)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE_STATUS);
			}else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check unique
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
					String accountNo = (String) accountMap.get("mainAccountNo");
					String productCode = (String) accountMap.get("productCode");
					
					validateDetail(corporateId, accountNo, productCode);
				}

			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
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

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CORP_ID));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("VARegistrationSC");

		return vo;
	}

	private VARegistrationModel setMapToModel(Map<String, Object> map, String corporateId) throws Exception {
		VARegistrationModel vaRegistration = new VARegistrationModel();

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		vaRegistration.setCorporate(corporate);

		AccountModel mainAccount = new AccountModel();
		mainAccount.setAccountNo(((String) map.get("mainAccountNo")));
		vaRegistration.setMainAccountNo(mainAccount);
		vaRegistration.setProductCode((String) map.get("productCode"));
		vaRegistration.setProductName((String) map.get("productName"));
		
		vaRegistration.setIdx((Integer) map.get("idx"));

		return vaRegistration;
	}

	private void checkUniqueRecord(String corporateId, String accountNo, String productCode) throws Exception {
		if (vaRegistrationRepo.findByCorporateIdAndAccountNo(corporateId, accountNo,productCode) != null) {
			throw new BusinessException("GPT-0100222");
		}
	}
	
	@Override
	public void validateDetail(String corporateId, String accountNo, String productCode) 
			throws ApplicationException, BusinessException {
		try {
			CorporateModel corporate = corporateRepo.findOne(corporateId);
			if(corporate == null || !VAStatus.Registered.name().equals(corporate.getVaStatus())) {
				throw new BusinessException("GPT-0100223");
			}
			
			checkUniqueRecord(corporateId, accountNo, productCode);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
			CorporateModel corporate = corporateRepo.findOne(corporateId);

			if (ApplicationConstants.WF_ACTION_UPDATE_STATUS.equals(vo.getAction())) {
				//update VA Status
				String vaStatus = (String) map.get("vaStatus");
				
				if(VAStatus.Registered.name().equals(vaStatus)) {
					//get corpProductCode from HOST
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("corporateId", corporateId);
					inputs.put("cif", corporate.getHostCifId());
					Map<String, Object> outputs = eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_REGISTRATION, inputs);
					
					String corpProductCode = (String) outputs.get("corpProductCode");
					corporate.setVaProductCode(corpProductCode);
					corporate.setVaStatus(vaStatus);
					corporateRepo.save(corporate);
				} else {
					List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
					
					List<Map<String, Object>> accounts = new ArrayList<>();
					
					if (accountList !=null) {
						for (Map<String, Object> accountMap : accountList) {
							String id = (String) accountMap.get("id");
							
							VARegistrationModel vaRegistration = vaRegistrationRepo.findOne(id);
							Map<String, Object> account = new HashMap<>();
							account.put("mainAccountNo", vaRegistration.getMainAccountNo().getAccountNo());
							account.put("productCode", vaRegistration.getProductCode());
							account.put("productName", vaRegistration.getProductName());
							accounts.add(account);
							
							List<VARegistrationDetailModel> vaRegistrationDetail = vaRegistrationRepo.findVADetailListByVARegisId(id, PagingUtils.createPageRequest(map)).getContent();
							
							//delete to HOST
							for (int i = 0; i < vaRegistrationDetail.size(); i++) {
								Map<String, Object> inputs = new HashMap<>();
								VARegistrationDetailModel detail = vaRegistrationDetail.get(i);
								
								inputs.put("cif", corporate.getHostCifId());
								inputs.put("corporateId", corporateId);
								inputs.put("productCode", vaRegistration.getProductCode());
								inputs.put("vaNo", detail.getVaNo().substring(5)); //delete prefix(corpCode+productCode)
								inputs.put("vaName", detail.getVaNo());
								inputs.put("userId", vo.getCreatedBy());
								eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_ACCOUNT_UNREGISTRATION, inputs);
							}
							
							vaRegistrationRepo.deleteDetailByVAId(id);
							
							
						}						
					}
					
					vaRegistrationRepo.deleteByCorporateId(corporateId);
					corporate.setVaProductCode(null);
					corporate.setVaStatus(vaStatus);
					corporateRepo.save(corporate);
						
					/*//unregis to HOST
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("corporateId", corporateId);
					inputs.put("accountList", accounts);
					eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_UNREGISTRATION, inputs);*/
				}
				
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					VARegistrationModel vaRegistration = setMapToModel(accountMap, corporateId);
					saveVARegistration(vaRegistration, vo.getCreatedBy());
				}
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("accountList", accountList);
				inputs.put("corporateId", corporateId);
				inputs.put("corpProductCode", corporate.getVaProductCode());
				eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_PRODUCT_REGISTRATION, inputs);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
				
				List<Map<String, Object>> accounts = new ArrayList<>();
				for (Map<String, Object> accountMap : accountList) {
					String id = (String) accountMap.get("id");
					
					
					VARegistrationModel vaRegistration = vaRegistrationRepo.findOne(id);
					Map<String, Object> account = new HashMap<>();
					account.put("mainAccountNo", vaRegistration.getMainAccountNo().getAccountNo());
					account.put("productCode", vaRegistration.getProductCode());
					account.put("productName", vaRegistration.getProductName());
					accounts.add(account);
					
					deleteVARegistration(id);
				}
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("accountList", accounts);
				inputs.put("corporateId", corporateId);
				inputs.put("corpProductCode", corporate.getVaProductCode());
				eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_PRODUCT_UNREGISTRATION, inputs);

			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private void saveVARegistration(VARegistrationModel vaRegistration, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		vaRegistration.setCreatedDate(DateUtils.getCurrentTimestamp());
		vaRegistration.setCreatedBy(createdBy);

		vaRegistrationRepo.persist(vaRegistration);
	}

	private void deleteVARegistration(String id)
			throws ApplicationException, BusinessException {
		vaRegistrationRepo.delete(id);
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return null;
	}

	@Override
	public Map<String, Object> searchMainAccountByCorporate(Map<String, Object> map) 
			throws ApplicationException, BusinessException {
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			CorporateModel corporate = corporateRepo.findOne(loginCorporateId);
			
			List<Map<String, Object>> corporateUserGroupAccountList = (List<Map<String,Object>>) corporateAccountGroupService.
					searchCorporateAccountGroupDetailForDebitOnlyGetMap(loginCorporateId, loginUserCode).get("accounts");
			
			List<String> accountList = new ArrayList<>();
			Map<String, Object> userGroupAccountMap = new HashMap<>();
			for(Map<String, Object> corporateUserGroupAccount : corporateUserGroupAccountList) {
				String accountNo = (String) corporateUserGroupAccount.get("accountNo");
				String accountGroupDtlId = (String) corporateUserGroupAccount.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
				accountList.add(accountNo);
				userGroupAccountMap.put(accountNo, accountGroupDtlId);
			}
			
			List<String> registeredAccounts = vaRegistrationRepo.findByRegisteredAccountNo(loginCorporateId, accountList);
			
			
			List<Map<String, Object>> accountListMap = new ArrayList<>();
			for(String accountStr : registeredAccounts) {
				AccountModel account = accountRepo.findOne(accountStr);
				
				Map<String, Object> accountMap = new HashMap<>();
				accountMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, userGroupAccountMap.get(account.getAccountNo()));				
				accountMap.put("accountNo", account.getAccountNo());
				accountMap.put("accountName", account.getAccountName());
				accountMap.put("accountCurrencyCode", account.getCurrency().getCode());
				accountMap.put("accountCurrencyName", account.getCurrency().getName());
				accountMap.put("corpProductCode", corporate.getVaProductCode());
				accountListMap.add(accountMap);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("accounts", accountListMap);
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchProductCodeByRegisteredAccountNo(Map<String, Object> map) 
			throws ApplicationException, BusinessException {
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDtlId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			
			CorporateAccountGroupDetailModel corpAcctGrpDtl = corporateAccountGroupRepo.findDetailById(loginCorporateId, accountGroupDtlId);
			
			List<String> accountList = new ArrayList<>();
			accountList.add(corpAcctGrpDtl.getCorporateAccount().getAccount().getAccountNo());
			
			List<String> productCodeList = vaRegistrationRepo.findProductCodeByRegisteredAccountNo(loginCorporateId, accountList);
			
			Map<String, Object> result = new HashMap<>();
			result.put("productCodes", productCodeList);
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> searchCorporateProducts(String cifId) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("cifId", cifId);
			return eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_PRODUCT_INQUIRY, inputs);
		
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
