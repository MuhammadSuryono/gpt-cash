package com.gpt.product.gpcash.corporate.corporate.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.userlock.services.IDMUserLockService;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.maintenance.branch.services.BranchService;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.services.TokenUserService;
import com.gpt.product.gpcash.servicepackage.services.ServicePackageService;

@Validate
@Service
public class CorporateSCImpl implements CorporateSC {
	
	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private IDMUserLockService idmUserLockService;
	
	@Autowired
	private BranchService branchService;
	
	@Autowired
	private ServicePackageService servicePackageService;
	
	@Autowired
	private TokenUserService tokenUserService;
	
	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, required = false, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = {
				menuCode,
				"MNU_GPCASH_OUTSOURCE_ADMIN"
		})
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cifId"), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "cifId"), 
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "address1", required = false),
		@Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "cityCode", required = false),
		@Variable(name = "stateCode", required = false),
		@Variable(name = "substateCode", required = false),
		@Variable(name = "postcode", required = false),
		@Variable(name = "countryCode", required = false)
	})	
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.searchOnline(map);
	}	

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "cifId"),
		@Variable(name = "address1"),
		@Variable(name = "address2"),
		@Variable(name = "address3"),
		@Variable(name = "postcode"),
		@Variable(name = "cityCode"),
		@Variable(name = "substateCode"),
		@Variable(name = "stateCode"),
		@Variable(name = "countryCode"),
		@Variable(name = "email1"),
		@Variable(name = "email2", required = false),
		@Variable(name = "phoneNo"),
		@Variable(name = "extNo", required = false),
		@Variable(name = "faxNo", required = false),
		@Variable(name = "branchCode"),
		@Variable(name = "servicePackageCode"),
		@Variable(name = "industrySegmentCode", required = false),
		@Variable(name = "businessUnitCode", required = false),
		@Variable(name = "taxIdNo"),
		@Variable(name = "handlingOfficerCode", required = false),
		@Variable(name = "maxCorporateUser"),
		@Variable(name = "lldIsResidence"),
		@Variable(name = "lldIsCitizen"),
		@Variable(name = "citizenCountryCode"),
		@Variable(name = "residenceCountryCode"),
		@Variable(name = "specialChargeFlag", options= {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "specialLimitFlag", options= {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "contactList", required = false, type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.STR_NAME, required = false),
			@SubVariable(name = "phoneNo", required = false),
			@SubVariable(name = "mobileNo", required = false),
			@SubVariable(name = "email", required = false, format = Format.EMAIL),
			@SubVariable(name = "faxNo", required = false),
		}),
		@Variable(name = "tokenList", required = false, type = List.class),
		@Variable(name = "adminList", required = false, type = List.class, subVariables = {
			@SubVariable(name = "userId", format = Format.UPPER_CASE),
			@SubVariable(name = ApplicationConstants.STR_NAME),
			@SubVariable(name = "mobileNo"),
			@SubVariable(name = "email", format = Format.EMAIL),
			@SubVariable(name = "tokenNo", required = false),
			@SubVariable(name = "tokenType", required = false, options = ApplicationConstants.TOKEN_TYPE_HARD_TOKEN)
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_CREATE, 
			ApplicationConstants.WF_ACTION_UPDATE
		}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.submit(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DELETE}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateService.reject(vo);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> getContactList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.getContactList(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "userId", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_RESET}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		String userCode = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.CORP_ID), (String) map.get("userId"));
		idmUserService.resetUser(userCode, (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100082");
		return resultMap;
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME),
		@Variable(name = "inactiveFlag", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "inactiveReason"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"UPDATE_STATUS"}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> updateStatusCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchBranch(Map<String, Object> map) throws ApplicationException, BusinessException {
		return branchService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchServicePackageForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return servicePackageService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "userId", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UNLOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		String userCode = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.CORP_ID), (String) map.get("userId"));
		idmUserLockService.unlockUser(userCode);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100084");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "userId", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_LOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		String userCode = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.CORP_ID), (String) map.get("userId"));
		idmUserLockService.lockUser(userCode);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100085");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "tokenType", options = ApplicationConstants.TOKEN_TYPE_HARD_TOKEN),
		@Variable(name = "tokenNo"),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = "tokenType"),
		@Variable(name = "tokenNo")
	})	
	@Override
	public Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		return tokenUserService.searchToken(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "tokenType", options = ApplicationConstants.TOKEN_TYPE_HARD_TOKEN),
		@Variable(name = "adminId", format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchTokenUserForEditAdmin(Map<String, Object> map) throws ApplicationException, BusinessException {
		String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
		
		List<String> tokenUserResult = new ArrayList<>();
		
		//add admin token
		String adminUserCode = Helper.getCorporateUserCode(corporateId, (String) map.get("adminId"));
		TokenUserModel adminToken = tokenUserService.findByAssignedUserCode(adminUserCode, false);
		
		if(adminToken != null) {
			tokenUserResult.add(adminToken.getTokenNo());
		}
		//------------------------------------------------
		
		//add all unassign token from corporateId
		List<String> listToken = corporateUserService.searchUnassignTokenUserByCorporateId(corporateId,
				(String) map.get("tokenType"));
		
		tokenUserResult.addAll(listToken);
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("tokenList", tokenUserResult);
		return resultMap;
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
}
