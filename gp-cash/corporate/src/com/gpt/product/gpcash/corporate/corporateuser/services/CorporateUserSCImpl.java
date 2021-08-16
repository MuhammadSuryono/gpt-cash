package com.gpt.product.gpcash.corporate.corporateuser.services;

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
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.services.AuthorizedLimitSchemeService;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.services.TokenUserService;

@Validate
@Service
public class CorporateUserSCImpl implements CorporateUserSC{

	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@Autowired
	private AuthorizedLimitSchemeService authorizedLimitSchemeService;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private TokenUserService tokenUserService;

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "userId", required = false, format = Format.UPPER_CASE),
		@Variable(name = "userName", required = false), 
		@Variable(name = "userGroupCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateUserService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "userId", format = Format.UPPER_CASE), 
		@Variable(name = "userName"), 
		@Variable(name = "email", format = Format.EMAIL),
		@Variable(name = "isNotifyMyTask", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "isNotifyMyTrx", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "mobileNo"),
		@Variable(name = "wfRoleCode"),
		@Variable(name = "wfRoleName"),
		@Variable(name = "authorizedLimitId"),
		@Variable(name = "authorizedLimitAlias"),
		@Variable(name = "userGroupCode"),
		@Variable(name = "userGroupName"),
		@Variable(name = "isGrantViewDetail", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "tokenType", required = false, format = Format.UPPER_CASE, options = ApplicationConstants.TOKEN_TYPE_HARD_TOKEN),
		@Variable(name = "tokenNo", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
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
		return corporateUserService.submit(map);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateUserService.approve(vo);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateUserService.reject(vo);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "userId", format = Format.UPPER_CASE), 
		@Variable(name = "userName"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserService.submit(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchWFRole(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserService.searchWorkflowRoleByApplicationCode(ApplicationConstants.APP_GPCASHIB);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchAuthorizedLimitScheme(Map<String, Object> map) throws ApplicationException, BusinessException {
		return authorizedLimitSchemeService.searchAuthorizedLimitSchemeByCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}

	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchUserGroup(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.searchUserGroupByCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchTokenUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		List<String> tokenUserResult = corporateUserService.searchUnassignTokenUserByCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
				(String) map.get("tokenType"));
		
		resultMap.put("result", tokenUserResult);
		
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "userId", format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> searchTokenUserForEditUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		
		List<String> tokenUserResult = new ArrayList<>();
		
		//add user token
		String userCode = Helper.getCorporateUserCode(corporateId, (String) map.get("userId"));
		TokenUserModel userToken = tokenUserService.findByAssignedUserCode(userCode, false);
		if(userToken != null) {
			tokenUserResult.add(userToken.getTokenNo());
		}
		
		tokenUserResult.addAll(corporateUserService.searchUnassignTokenUserByCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
				(String) map.get("tokenType")));
		
		resultMap.put("result", tokenUserResult);
		
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "resetUserCode", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_RESET}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> resetUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		String userCodeForReset = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get("resetUserCode"));
		idmUserService.resetUser(userCodeForReset, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
		
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100082");
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "userIdList", type = List.class, subVariables = {
			@SubVariable(name = "userId")
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_LIST"}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDeleteList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserService.submit(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "unlockUserCode", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UNLOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> unlockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		String userCodeForUnlock = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get("unlockUserCode"));
		idmUserService.unlockUser(userCodeForUnlock, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
		
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100081");
		return resultMap;
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "lockUserCode", format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_LOCK}), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> lockUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		String userCodeForLock = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get("lockUserCode"));
		
		idmUserService.lockUser(userCodeForLock, (String) map.get(ApplicationConstants.LOGIN_USERID));
		
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100085");
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE)
	})
	@Override
	public Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = corporateUserService.getUserProfiles(map);
		
		if(resultMap != null) {
			resultMap.put(ApplicationConstants.LOGIN_MENULIST, ((HashMap<?,?>) idmLoginService.getProfiles((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get("passwd"), true)).get("loginMenuTree"));
			
			List<Map<String, Object>> menuList = (ArrayList<Map<String,Object>>)resultMap.get(ApplicationConstants.LOGIN_MENULIST);
			
			resultMap.put("isShowBeneficiary", ApplicationConstants.NO);
			for(Map<String, Object> menuMap : menuList) {
				//requested by Susi untuk keperluan UI
				if(menuMap.get("menuCode").equals("MNU_GPCASH_F_FUND_BENEFICIARY") || menuMap.get("menuCode").equals("MNU_GPCASH_F_BENEFICIARY")) {
					resultMap.put("isShowBeneficiary", ApplicationConstants.YES);
					break;
				}
			}
		}
		
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "oldPassword"),
		@Variable(name = "newPassword"),
		@Variable(name = "newPassword2"),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {"RESET"}),
		@Variable(name = "key", required = false) //sengaja dibuat false agar tidak di validate 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> forceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userId = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String userCode = Helper.getCorporateUserCode(corporateId, userId);
		
		//replacing to plainPasswd
		map.put("oldPassword", idmLoginService.getPlainPasswd((String) map.get("oldPassword"), (String) map.get("key")));
		map.put("newPassword", idmLoginService.getPlainPasswd((String) map.get("newPassword"), (String) map.get("key")));
		map.put("newPassword2", idmLoginService.getPlainPasswd((String) map.get("newPassword2"), (String) map.get("key")));
		//--------------------------
		
		idmUserService.changePassword(userCode, (String) map.get("oldPassword"), (String) map.get("newPassword"),
				(String) map.get("newPassword2"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100116");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = "email", format = Format.EMAIL)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userId = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String userCode = Helper.getCorporateUserCode(corporateId, userId);
		corporateUserService.forgotPassword(corporateId, userCode, (String) map.get("email"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100082");
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = "isNotifyMyTask", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = "isNotifyMyTrx", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_UPDATE}),
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
	})
	@Override
	public Map<String, Object> updateUserNotificationFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		corporateUserService.updateUserNotificationFlag((String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get("isNotifyMyTask"), 
				(String) map.get("isNotifyMyTrx"));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100152");
		return resultMap;
	}
}