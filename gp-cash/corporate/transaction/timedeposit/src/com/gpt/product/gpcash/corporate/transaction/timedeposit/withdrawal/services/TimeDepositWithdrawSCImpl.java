package com.gpt.product.gpcash.corporate.transaction.timedeposit.withdrawal.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.constants.TimeDepositConstants;

@Validate
@Service
public class TimeDepositWithdrawSCImpl implements TimeDepositWithdrawSC {

	@Autowired
	private TimeDepositWithdrawService withdrawService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;

	@SuppressWarnings("unchecked")
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "timeDepositId"),
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = "product"),
		@Variable(name = "termParam", type = Integer.class, defaultValue = "0"),
		@Variable(name = "termType"),
		@Variable(name = "principalCurrency"), 
		@Variable(name = "principalAmount", type = BigDecimal.class),
		@Variable(name = "maturityInstruction", options = {
			TimeDepositConstants.MATURITY_INS_ARO, 
			TimeDepositConstants.MATURITY_INS_ARO_PRCPL,
			TimeDepositConstants.MATURITY_INS_NON_ARO
		}),
		@Variable(name = "placementDate", type = Timestamp.class),
		@Variable(name = "withdrawalDate", type = Timestamp.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CREATE}),
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
		return  withdrawService.submit(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = "product"),
		@Variable(name = "termParam", type = Integer.class, defaultValue = "0"),
		@Variable(name = "termType"),
		@Variable(name = "principalCurrency"), 
		@Variable(name = "principalAmount", type = BigDecimal.class),
		@Variable(name = "maturityInstruction", options = {
			TimeDepositConstants.MATURITY_INS_ARO, 
			TimeDepositConstants.MATURITY_INS_ARO_PRCPL,
			TimeDepositConstants.MATURITY_INS_NON_ARO
		}),
		@Variable(name = "placementDate", type = Timestamp.class),
		@Variable(name = "withdrawalDate", type = Timestamp.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N, required = false),
	})
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		return withdrawService.confirm(map);
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		vo = withdrawService.approve(vo);
		return vo;
	}

	@EnableCorporateActivityLog
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		return withdrawService.reject(vo);
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
		return corporateAccountGroupService.searchCorporateAccountGroupDetailForDebitOnlyGetMap((String)map.get(ApplicationConstants.LOGIN_CORP_ID), 
				(String)map.get(ApplicationConstants.LOGIN_USERCODE));
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "receiptId"),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		return withdrawService.downloadTransactionStatus(map);
	}
	
	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_GRP_DTL_ID),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_SEARCH
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return withdrawService.search(map);
	}
	
	@EnableCorporateActivityLog
	@Input({
		@Variable(name = "id"),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {
			ApplicationConstants.WF_ACTION_DETAIL
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> detailTimeDeposit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return withdrawService.detailTimeDeposit(map);
	}
}