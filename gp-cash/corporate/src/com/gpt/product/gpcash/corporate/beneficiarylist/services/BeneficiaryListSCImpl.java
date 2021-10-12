package com.gpt.product.gpcash.corporate.beneficiarylist.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.maintenance.domesticbank.services.DomesticBankService;
import com.gpt.component.maintenance.internationalbank.services.InternationalBankService;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.token.validation.services.TokenValidationService;

@Validate
@Service
public class BeneficiaryListSCImpl implements BeneficiaryListSC {

	@Autowired
	private BeneficiaryListInHouseService beneficiaryListInHouseService;

	@Autowired
	private BeneficiaryListDomesticService beneficiaryListDomesticService;

	@Autowired
	private BeneficiaryListInternationalService beneficiaryListInternationalService;
	
	@Autowired
	private DomesticBankService domesticBankService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@Autowired
	private InternationalBankService internationalBankService;

	@Autowired
	private TokenValidationService tokenValidationService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	// -----------------------------------InHouse----------------------------------------------------
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return beneficiaryListInHouseService.search(map);
	}

	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName") 
	})
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.searchOnline(map);
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			Map<String, Object> challengeMap = getChallenge(map);
			resultMap.putAll(challengeMap);
		}
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName"),
		@Variable(name = "benAccountCurrency"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", format = Format.EMAIL, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_OVERBOOKING", "UPDATE_OVERBOOKING" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.NO.equals(vo.getIsError()) ||vo.getIsError() == null ) {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			} else {
				throw new BusinessException(vo.getErrorCode());
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		if ("CREATE_DOMESTIC".equals(vo.getAction()) || "UPDATE_DOMESTIC".equals(vo.getAction()) ||
				"DELETE_DOMESTIC".equals(vo.getAction()) || "DELETE_DOMESTIC_LIST".equals(vo.getAction()) || 
				"CREATE_DOMESTIC_ONLINE".equals(vo.getAction()) || "UPDATE_DOMESTIC_ONLINE".equals(vo.getAction()) ||
				"DELETE_DOMESTIC_ONLINE".equals(vo.getAction()) || "DELETE_DOMESTIC_ONLINE_LIST".equals(vo.getAction())) {
			return beneficiaryListDomesticService.approve(vo);
		} else if ("CREATE_INTERNATIONAL".equals(vo.getAction()) || "UPDATE_INTERNATIONAL".equals(vo.getAction()) ||
				"DELETE_INTERNATIONAL".equals(vo.getAction()) || "DELETE_INTERNATIONAL_LIST".equals(vo.getAction())) {
			return beneficiaryListInternationalService.approve(vo); 
		} else {
			return beneficiaryListInHouseService.approve(vo);
		}
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		if ("CREATE_DOMESTIC".equals(vo.getAction()) || "UPDATE_DOMESTIC".equals(vo.getAction()) ||
				"DELETE_DOMESTIC".equals(vo.getAction()) || "DELETE_DOMESTIC_LIST".equals(vo.getAction())) {
			return beneficiaryListDomesticService.reject(vo);
		} else {
			return beneficiaryListInHouseService.reject(vo);
		}
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_OVERBOOKING" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_OVERBOOKING_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDeleteList(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		return resultMap;
	}

	// -----------------------------------Domestic----------------------------------------------------
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = "bankCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return beneficiaryListDomesticService.search(map);		
	}

	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = "bankCode"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName") 
	})
	@Override
	public Map<String, Object> searchOnlineDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));

		Map<String, Object> resultMap =  beneficiaryListDomesticService.searchOnline(map);
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			Map<String, Object> challengeMap = getChallenge(map);
			resultMap.putAll(challengeMap);
		}
		
		return resultMap;
		
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName"), @Variable(name = "benAliasName"),
		@Variable(name = "address1", required = false), @Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benCitizenCountryCode", required = false), @Variable(name = "beneficiaryTypeCode"),
		@Variable(name = "bankCode"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", required = false, format = Format.EMAIL),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_DOMESTIC", "UPDATE_DOMESTIC" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.NO.equals(vo.getIsError()) ||vo.getIsError() == null ) {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			} else {
				throw new BusinessException(vo.getErrorCode());
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_DOMESTIC", "DELETE_DOMESTIC_ONLINE" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDeleteDomestic(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_DOMESTIC_LIST", "DELETE_DOMESTIC_ONLINE_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDeleteDomesticList(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBankForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return domesticBankService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_BENEFICIARY_TYP"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_COUNTRY"); 
		return parameterMaintenanceService.searchModel(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = "bankCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
		// put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return beneficiaryListInternationalService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = "benAccountName"),
		@Variable(name = "benAliasName"),
		@Variable(name = "address1", required = false), 
		@Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benCitizenCountryCode", required = false),
		@Variable(name = "bankCode"),
		@Variable(name = "intCountryCode"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", required = false, format = Format.EMAIL),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_INTERNATIONAL", "UPDATE_INTERNATIONAL" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		Map<String, Object> resultMap = beneficiaryListInternationalService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.NO.equals(vo.getIsError()) ||vo.getIsError() == null ) {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			} else {
				throw new BusinessException(vo.getErrorCode());
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_INTERNATIONAL" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDeleteInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListInternationalService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_INTERNATIONAL_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDeleteInternationalList(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneficiaryListInternationalService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError()) ) {
				throw new BusinessException(vo.getErrorCode());
			} else {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "countryCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL)
	})
	@Override
	public Map<String, Object> searchBankByCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalBankService.search(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName"), @Variable(name = "benAliasName"),
		@Variable(name = "bankCode"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", required = false, format = Format.EMAIL),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_DOMESTIC_ONLINE", "UPDATE_DOMESTIC_ONLINE" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submitDomesticOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.NO.equals(vo.getIsError()) ||vo.getIsError() == null ) {
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			} else {
				throw new BusinessException(vo.getErrorCode());
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);
			
		}
		
		return resultMap;
	}
	
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName") 
	})
	@Override
	public Map<String, Object> searchVirtualAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap =  beneficiaryListInHouseService.inquiryVirtualAccount(map);
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			Map<String, Object> challengeMap = getChallenge(map);
			resultMap.putAll(challengeMap);
		}
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> getChallenge(Map<String, Object> map) throws ApplicationException, BusinessException {
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String tokenNo = (String) map.get(ApplicationConstants.LOGIN_TOKEN_NO);
		
		map.put(ApplicationConstants.IS_ONE_SIGNER, isOneSigner);
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
			if(!ValueUtils.hasValue(tokenNo)) {
				throw new BusinessException("GPT-0100153");
			}
			map.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge(corpId,userCode,tokenNo));				
		}
		
		return map; 
	}
}
