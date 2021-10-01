package com.gpt.product.gpcash.corporate.transaction.va.accountlist.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.account.repository.AccountRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.va.accountlist.VADetailStatus;
import com.gpt.product.gpcash.corporate.transaction.va.registration.constants.VARegistrationConstants;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationDetailModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.repository.VARegistrationRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class VAAccountListServiceImpl implements VAAccountListService {
	@Autowired
    private AccountRepository accountRepo;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
    private VARegistrationRepository vaRegistrationRepo;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private CorporateRepository corporateRepo;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) 
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			String mainAccountNo = (String) map.get("mainAccountNo");
			
			List<String> accountList = new ArrayList<>();
			if(ValueUtils.hasValue(mainAccountNo)) {
				//filter by input mainAccountNo
				accountList.add(mainAccountNo);
			} else {
				List<Map<String, Object>> corporateUserGroupAccountList = (ArrayList) corporateAccountGroupService.
						searchCorporateAccountGroupDetailForDebitOnlyGetMap(loginCorporateId, loginUserCode).get("accounts");
				
				for(Map<String, Object> corporateUserGroupAccount : corporateUserGroupAccountList) {
					accountList.add((String) corporateUserGroupAccount.get("accountNo"));
				}
			}
			
			Page<VARegistrationModel> result = vaRegistrationRepo.
					findByCorporateIdAndAccountNo(loginCorporateId, accountList, PagingUtils.createPageRequest(map));

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
	
	private List<Map<String, Object>> setModelToMap(List<VARegistrationModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (VARegistrationModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(VARegistrationModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		
		map.put("vaRegistrationId", model.getId());
		map.put("productCode", model.getProductCode());
		map.put("productName", model.getProductName());
		
		AccountModel account = model.getMainAccountNo();
		map.put("mainAccountNo", account.getAccountNo());
		map.put("mainAccountName", account.getAccountName());
		map.put("mainAccountCurrencyCode", account.getCurrency().getCode());
		
		CorporateModel corporate = model.getCorporate();
		map.put("corpProductCode", corporate.getVaProductCode());
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		
		return map;
	}
	
	@Override
	public Map<String, Object> searchMainAccount(Map<String, Object> map) 
			throws ApplicationException, BusinessException {
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			List<Map<String, Object>> corporateUserGroupAccountList = (ArrayList) corporateAccountGroupService.
					searchCorporateAccountGroupDetailForDebitOnlyGetMap(loginCorporateId, loginUserCode).get("accounts");
			
			List<String> accountList = new ArrayList<>();
			for(Map<String, Object> corporateUserGroupAccount : corporateUserGroupAccountList) {
				accountList.add((String) corporateUserGroupAccount.get("accountNo"));
			}
			
			List<String> registeredAccounts = vaRegistrationRepo.findByRegisteredAccountNo(loginCorporateId, accountList);
			
			
			List<Map<String, Object>> accountListMap = new ArrayList<>();
			for(String accountStr : registeredAccounts) {
				AccountModel account = accountRepo.findOne(accountStr);
				
				Map<String, Object> accountMap = new HashMap<>();
				accountMap.put("accountNo", account.getAccountNo());
				accountMap.put("accountName", account.getAccountName());
				accountMap.put("accountCurrencyCode", account.getCurrency().getCode());
				accountMap.put("accountCurrencyName", account.getCurrency().getName());
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
	public Map<String, Object> searchVADetailList(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String vaRegistrationId = (String) map.get("vaRegistrationId");
			
			Page<VARegistrationDetailModel> result = vaRegistrationRepo.findVADetailListByVARegisId(vaRegistrationId, 
					PagingUtils.createPageRequest(map));

			resultMap.put("result", setDetailModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setDetailModelToMap(List<VARegistrationDetailModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (VARegistrationDetailModel model : list) {
			resultList.add(setDetailModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setDetailModelToMap(VARegistrationDetailModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		map.put("vaNo", model.getVaNo());
		map.put("vaName", model.getVaName());
		
		Locale locale = LocaleContextHolder.getLocale();
		map.put("vaStatus", message.getMessage(model.getVaStatus(), null, locale));
		
		map.put("vaType", model.getVaType());
		map.put("vaAmount", model.getVaAmount());
		
		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {

				String vaNo = (String) map.get("vaNo");
				
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord(vaNo, corporateId);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE_LIST)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE_LIST);

				List<Map<String, Object>> vaDetailList = (ArrayList<Map<String,Object>>)map.get("vaDetailList");
				for(Map<String, Object> vaDetailMap : vaDetailList) {
					
					// check existing record exist or not
					getExistingRecord((String) vaDetailMap.get("vaNo"), true);
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

	private VARegistrationDetailModel getExistingRecord(String vaNo, boolean isThrowError) throws Exception {
		VARegistrationDetailModel model = vaRegistrationRepo.findVADetailListByVANo(vaNo);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} 

		return model;
	}

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("VAAccountListSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		//set userGroupId
		vo.setUserGroupId(userGroupId);
		
		if(map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
			String vaNo = (String) map.get("vaNo");
			String uniqueKey = corporateId.concat(vaNo);
			
			vo.setUniqueKey(uniqueKey);
			vo.setUniqueKeyDisplay(vaNo);
			
			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
			
		} else {
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			List<Map<String, Object>> vaDetailList = (ArrayList<Map<String,Object>>) map.get("vaDetailList");
			for(Map<String, Object> vaDetailMap : vaDetailList){
				String uniqueKey = corporateId.concat((String) vaDetailMap.get("vaNo"));
				uniqueKeyAppend = uniqueKeyAppend + uniqueKey + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(uniqueKey);
				pendingTaskService.checkUniquePendingTaskLike(vo);
			}
			
			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
			
		}

		return vo;
	}
	
	private void checkUniqueRecord(String vaNo, String corporateId) throws Exception {
		VARegistrationDetailModel model = vaRegistrationRepo.findVADetailListByVANo(vaNo);

		if (model != null) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			CorporateModel corporate = corporateRepo.findOne(corporateId);
			CorporateUserModel corpUser = corporateUtilsRepo.getCorporateUserRepo().findOne(vo.getCreatedBy());

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				String vaNo = (String) map.get("vaNo");
				String vaName = (String) map.get("vaName");
				String vaType = (String) map.get("vaType");
				BigDecimal vaAmount = new BigDecimal(map.get("vaAmount").toString());

				String vaRegistrationId = (String) map.get("vaRegistrationId");
				
				VARegistrationModel vaRegistration = vaRegistrationRepo.findOne(vaRegistrationId);
				
				VARegistrationDetailModel vaRegistrationDetail = new VARegistrationDetailModel();
				vaRegistrationDetail.setVaNo(vaNo);
				vaRegistrationDetail.setVaName(vaName);
				vaRegistrationDetail.setVaType(vaType);
				vaRegistrationDetail.setVaAmount(vaAmount);
				vaRegistrationDetail.setVaStatus(VADetailStatus.ACTIVE.name());
				vaRegistrationDetail.setCreatedBy(vo.getCreatedBy());
				vaRegistrationDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
				vaRegistrationDetail.setVaRegistration(vaRegistration);
				vaRegistrationDetail.setIdx(ApplicationConstants.IDX);
				
				vaRegistration.getVaRegistrationDetail().add(vaRegistrationDetail);
				vaRegistrationRepo.save(vaRegistration);
				
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("cif", corporate.getHostCifId());
				inputs.put("corporateId", corporateId);
				inputs.put("productCode", vaRegistration.getProductCode());
				inputs.put("vaNo", vaNo.substring(5)); //delete prefix(corpCode+productCode)
				inputs.put("vaName", vaName);
				inputs.put("vaType", vaType.toLowerCase());
				inputs.put("amount", vaAmount);
				inputs.put("userId", corpUser.getUserId());
				eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_ACCOUNT_REGISTRATION, inputs);
			} else if(ApplicationConstants.WF_ACTION_DELETE_LIST.equals(vo.getAction())) {
				List<Map<String, Object>> vaDetailList = (ArrayList<Map<String,Object>>)map.get("vaDetailList");
				Map<String, Object> detail_params = (Map<String, Object>) map.get("detail_params");
				
				for(Map<String, Object> vaDetailMap : vaDetailList) {
					String vaNo = (String) vaDetailMap.get("vaNo");
					String vaName = (String) vaDetailMap.get("vaName");
					
					//validate record still exist or not
					getExistingRecord(vaNo, true);
					
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("cif", corporate.getHostCifId());
					inputs.put("corporateId", corporateId);
					inputs.put("productCode", detail_params.get("productCode"));
					inputs.put("vaNo", vaNo.substring(5)); //delete prefix(corpCode+productCode)
					inputs.put("vaName", vaName);
					inputs.put("userId", corpUser.getUserId());
					eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_ACCOUNT_UNREGISTRATION, inputs);
					
					vaRegistrationRepo.deleteDetailByVANo(vaNo);
					
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
}
