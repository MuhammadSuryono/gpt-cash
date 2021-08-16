package com.gpt.product.gpcash.bankapprovalmatrix.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
@Transactional(rollbackFor = Exception.class)
public class BankApprovalMatrixSCImpl implements BankApprovalMatrixSC {
	@Autowired
	private BankApprovalMatrixService bankApprovalMatrixService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "approvalMatrixMenuCode", required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.search(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "id"),
		@Variable(name = "noOfApprover"),
		@Variable(name = "approvalMatrixMenuCode"),
		@Variable(name = "bankApprovalMatrixDetailList", type = List.class, subVariables = {
			@SubVariable(name = "approvalLevelCode"),
			@SubVariable(name = ApplicationConstants.STR_MENUCODE),
			@SubVariable(name = "menuName"),
			@SubVariable(name = "noOfUser"),
			@SubVariable(name = "branchOpt")
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_UPDATE), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.reject(vo);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> getBankApprovalMatrixMenu(Map<String, Object> map) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.getBankApprovalMatrixMenu(map);
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "approvalMatrixMenuCode"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode), 
	})
	@Override
	public Map<String, Object> searchBankApprovalMatrixDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.searchBankApprovalMatrixDetail(map);
	}

	@Override
	public Map<String, Object> getApprovaLevelforDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return bankApprovalMatrixService.getApprovaLevelforDroplist(map);
	}
}
