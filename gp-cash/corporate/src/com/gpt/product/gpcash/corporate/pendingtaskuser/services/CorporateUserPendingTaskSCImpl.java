package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SortingItem;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.token.validation.services.TokenValidationService;

@Validate
@Service
public class CorporateUserPendingTaskSCImpl implements CorporateUserPendingTaskSC {

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateUserMultiPendingTaskService multiPendingTaskService;
	
	@Autowired
	private TokenValidationService tokenValidationService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingTaskService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL, sortingItems = {
		@SortingItem("currApprLvCount"),
		@SortingItem("currApprLv"),
		@SortingItem("startDate"),
		@SortingItem("referenceNo"),
		@SortingItem("actionBy"),
		@SortingItem("actionByName"),
		@SortingItem("actionDate"),
		@SortingItem("uniqueKeyDisplay"),
		@SortingItem("action"),
		@SortingItem("pendingTaskMenuName"),
		@SortingItem("debitAccount"),
		@SortingItem("debitAccountName"),
		@SortingItem("debitAccountCurrency"),
		@SortingItem("transactionAmount"),
		@SortingItem("transactionCurrency"),
		@SortingItem("creditAccount"),
		@SortingItem("creditAccountName"),
		@SortingItem("creditAccountCurrency")
	})
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "creationDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "creationDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateFrom", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "instructionDateTo", type = Timestamp.class, required = false, format = Format.DATE),
		@Variable(name = "pendingTaskMenuCode", required = false),
		@Variable(name = "corpAccount", required = false),
		@Variable(name = "trxAmountFrom", type = BigDecimal.class, required = false),
		@Variable(name = "trxAmountTo", type = BigDecimal.class, required = false),
	})
	@Override
	public Map<String, Object> searchPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskByUser(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
	})
	@Output(@Variable(name = "total", type = Integer.class))
	@Override
	public Map<String, Object> countPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.countPendingTaskByUser(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "stageId"),
		@Variable(name = "approvalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
		@Variable(name = "approvalLvAlias", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
		@Variable(name = "approvalLvCount"),
		@Variable(name = "approvalLvRequired"),
		@Variable(name = "startDate", type = Timestamp.class),
		@Variable(name = "tasks", type = List.class, subVariables = {
			@SubVariable(name = "userCode"),
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName"),
			@SubVariable(name = "userApprovalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
			@SubVariable(name = "userApprovalLvAlias", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
		})
	})
	@Override
	public Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskByReferenceNo(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = "activities", type = List.class, subVariables = {
			@SubVariable(name = "userCode"),
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName"),
			@SubVariable(name = "activity", format = Format.I18N),
			@SubVariable(name = "activityDate", type = Timestamp.class),
			@SubVariable(name = "amount", type = BigDecimal.class, required = false),
			@SubVariable(name = "amountCcyCd", required = false), //menu non transaction tidak ada amount
			@SubVariable(name = "approvalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
			@SubVariable(name = "approvalLvAlias", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
			@SubVariable(name = "approvalLvCount", type = Integer.class, required = false),
			@SubVariable(name = "approvalLvRequired", type = Integer.class, required = false),
			@SubVariable(name = "status", format = Format.I18N)
		})
	})
	@Override
	public Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskHistoryByReferenceNo(map);
	}
	
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.CHALLENGE_NO)
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
		if(!ValueUtils.hasValue((String) map.get(ApplicationConstants.LOGIN_TOKEN_NO))) {
			throw new BusinessException("GPT-0100153");
		}
		
		Map<String, Object> resultMap = pendingTaskService.detailPendingTask(map);
		
		resultMap.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO)));
		
		return resultMap;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERCODE, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = "CONFIRM"), 
		@Variable(name = "pendingTaskList",  type = List.class, subVariables = {
			@SubVariable(name = "taskId"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE)
		})
	})
	@Output({
		@Variable(name = ApplicationConstants.CHALLENGE_NO)
	})
	@Override
	public Map<String, Object> confirmList(Map<String, Object> map) throws ApplicationException, BusinessException {
		///pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
		if(!ValueUtils.hasValue((String) map.get(ApplicationConstants.LOGIN_TOKEN_NO))) {
			throw new BusinessException("GPT-0100153");
		}
				
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO)));
		resultMap.put("pendingTaskList", map.get("pendingTaskList"));
		return resultMap;
	}

	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> detailPendingTaskOld(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.detailPendingTaskOld(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_APPROVE),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) ,
		@Variable(name = "taskId"),
		@Variable(name = ApplicationConstants.LOGIN_TOKEN_NO),
		@Variable(name = ApplicationConstants.CHALLENGE_NO),
		@Variable(name = ApplicationConstants.RESPONSE_NO)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException {
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		
		return pendingTaskService.approve(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_REJECT),
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "taskId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException {
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		
		return pendingTaskService.reject(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_APPROVE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) ,
		@Variable(name = ApplicationConstants.LOGIN_TOKEN_NO),
		@Variable(name = ApplicationConstants.CHALLENGE_NO),
		@Variable(name = ApplicationConstants.RESPONSE_NO),
		@Variable(name = "pendingTaskList", type = List.class, subVariables = { 
			@SubVariable(name = "taskId"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE)
		}), 
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@SubVariable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
		})
	})
	@Override
	public Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException {
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		
		return multiPendingTaskService.approveList(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_REJECT),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) ,
		@Variable(name = "pendingTaskList", type = List.class, subVariables = { @SubVariable(name = "taskId"),
			@SubVariable(name = "taskId"),
			@SubVariable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, format = Format.UPPER_CASE)
		}) 
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
			@SubVariable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
		})
	})
	@Override
	public Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException {
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		
		return multiPendingTaskService.rejectList(map);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void expiredPendingTask(String parameter) throws ApplicationException, BusinessException {
		pendingTaskService.expiredPendingTask(parameter);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
			@SubVariable(name = "accountNo"),
			@SubVariable(name = "accountName"),
			@SubVariable(name = "accountTypeCode"),
			@SubVariable(name = "accountTypeName"),
			@SubVariable(name = "accountCurrencyCode"),
			@SubVariable(name = "accountCurrencyName"),
			@SubVariable(name = "accountBranchCode", required = false),
			@SubVariable(name = "accountBranchName", required = false)
		})			
	})	
	@Override
	public Map<String, Object> searchSourceAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateAccountGroupService.searchCorporateAccountGroupDetailForDebitOnlyMultiCurrencyGetMap((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "menus", type = List.class, subVariables = {
			@SubVariable(name = "pendingTaskCode"),
			@SubVariable(name = "pendingTaskName"),
		})			
	})	
	@Override
	public Map<String, Object> searchUserGroupMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateUserGroupService.findMenuForPendingTask((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}

}
