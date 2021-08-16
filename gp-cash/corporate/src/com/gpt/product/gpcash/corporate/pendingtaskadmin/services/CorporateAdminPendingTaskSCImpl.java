package com.gpt.product.gpcash.corporate.pendingtaskadmin.services;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.token.validation.services.TokenValidationService;

@Validate
@Service
public class CorporateAdminPendingTaskSCImpl implements CorporateAdminPendingTaskSC{

	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateAdminMultiPendingTaskService multiPendingTaskService;
	
	@Autowired
	private TokenValidationService tokenValidationService;
	
	@Autowired
	private CorporateService corporateService;
	
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
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "pendingTaskMenuCode", required = false)
	})
	@Override
	public Map<String, Object> searchPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return pendingTaskService.searchPendingTaskByUser(map);
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
		@Variable(name="stageId"),
		@Variable(name="approvalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
		@Variable(name="approvalLvCount"),
		@Variable(name="approvalLvRequired"),
		@Variable(name = "startDate", type = Timestamp.class),
		@Variable(name = "tasks", type = List.class, subVariables = {
			@SubVariable(name = "userCode"),
			@SubVariable(name = "userId"),
			@SubVariable(name = "userName")
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
			@SubVariable(name = "approvalLvName", required = false, defaultValue = ApplicationConstants.EMPTY_STRING),
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
		@Variable(name = ApplicationConstants.CHALLENGE_NO,required=false)
	})
	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);
		
		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
			if(!ValueUtils.hasValue((String) map.get(ApplicationConstants.LOGIN_TOKEN_NO))) {
				throw new BusinessException("GPT-0100153");
			}
		}
		Map<String, Object> resultMap = pendingTaskService.detailPendingTask(map);
		
		

		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			resultMap.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO)));
		}
		
		resultMap.put("tokenAuthFlag", corporate.getTokenAuthenticationFlag());
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
		@Variable(name = ApplicationConstants.CHALLENGE_NO,required=false)
	})
	@Override
	public Map<String, Object> confirmList(Map<String, Object> map) throws ApplicationException, BusinessException {
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);
		Map<String, Object> resultMap = new HashMap<>();
		
		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
			if(!ValueUtils.hasValue((String) map.get(ApplicationConstants.LOGIN_TOKEN_NO))) {
				throw new BusinessException("GPT-0100153");
			}
					
			
			resultMap.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO)));
		}
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
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "taskId"),
		@Variable(name = ApplicationConstants.LOGIN_TOKEN_NO,required=false),
		@Variable(name = ApplicationConstants.CHALLENGE_NO,required=false),
		@Variable(name = ApplicationConstants.RESPONSE_NO,required=false)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);

		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get("responseNo"));
		}
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
		@Variable(name = ApplicationConstants.CHALLENGE_NO,required=false),
		@Variable(name = ApplicationConstants.RESPONSE_NO,required=false),
		@Variable(name = "taskId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N, required = false)
	})
	@Override
	public Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException {
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);

		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get("responseNo"));
		}
		return pendingTaskService.reject(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_APPROVE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "pendingTaskList",  type = List.class, subVariables = {
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
	public Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);

		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
		tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get("responseNo"));
		}
		
		return multiPendingTaskService.approveList(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_REJECT), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "pendingTaskList",  type = List.class, subVariables = {
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
	public Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		CorporateModel corporate = corporateService.getExistingRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID), false);

		if(corporate!= null && !corporate.getTokenAuthenticationFlag().equals("N")) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
				(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get("responseNo"));
		}
		
		return multiPendingTaskService.rejectList(map);
	}

}
