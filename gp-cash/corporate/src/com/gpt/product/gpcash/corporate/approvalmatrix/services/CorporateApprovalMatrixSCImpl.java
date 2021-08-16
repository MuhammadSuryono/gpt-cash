package com.gpt.product.gpcash.corporate.approvalmatrix.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubSubVariable;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.services.AuthorizedLimitSchemeService;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

@Validate
@Service
public class CorporateApprovalMatrixSCImpl implements CorporateApprovalMatrixSC {

	@Autowired
	private CorporateApprovalMatrixService corporateApprovalMatrixService;
	
	@Autowired
	private CorporateUserGroupService corporateUserGroupService;
	
	@Autowired
	private AuthorizedLimitSchemeService authorizedLimitSchemeService;
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
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
		//put corporateId for searching based on login corporate id
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		return corporateApprovalMatrixService.search(map);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({ 
		@Variable(name = "searchMenuCode", format = Format.UPPER_CASE), 
		@Variable(name = "searchMenuName"),
		@Variable(name = "currencyCode", format = Format.UPPER_CASE, required = false),
		@Variable(name = "approvalMatrixListing", type = List.class , subVariables = {
			@SubVariable(name = "rangeLimit"),
			@SubVariable(name = "noOfApproval"),
			@SubVariable(name = "approvalList", type = List.class, subVariables = {
				@SubSubVariable(name = "sequenceNo", type = Integer.class),
				@SubSubVariable(name = "noOfUser", type = Integer.class),
				@SubSubVariable(name = "authorizedLimitId", required = false),
				@SubSubVariable(name = "userGroupOptionCode", options = { 
					ApplicationConstants.USER_GROUP_OPTION_ANY_GROUP, 
					ApplicationConstants.USER_GROUP_OPTION_INTRA_GROUP, 
					ApplicationConstants.USER_GROUP_OPTION_CROSS_GROUP, 
					ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP 
				}),
				@SubSubVariable(name = "userGroupCode", required = false)
			})
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_UPDATE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateApprovalMatrixService.submit(map);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateApprovalMatrixService.approve(vo);
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		return corporateApprovalMatrixService.reject(vo);
	}

	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "searchMenuCode", format = Format.UPPER_CASE),
		@Variable(name = "searchMenuName"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) })
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateApprovalMatrixService.submit(map);
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
	public Map<String, Object> getCurrency(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateApprovalMatrixService.getCurrency();
	}
}
