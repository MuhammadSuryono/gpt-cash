package com.gpt.product.gpcash.corporate.transaction.globaltransaction.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.account.repository.AccountRepository;
import com.gpt.product.gpcash.corporate.inquiry.balance.services.BalanceSummaryService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.model.GlobalTransactionModel;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.repository.GlobalTransactionRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.usertransactionmapping.services.UserTransactionMappingService;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class GlobalTransactionServiceImpl implements GlobalTransactionService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BalanceSummaryService balanceSummaryService;
	
	@Autowired
	private UserTransactionMappingService userTransactionMappingService;
	
	@Autowired
	private AccountRepository accountRepo;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private GlobalTransactionRepository globalTransactionRepo;
	
	@Override
	public Map<String, Object> checkBalance(String corporateId, String userCode, String accountGroupDtlId) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put(ApplicationConstants.LOGIN_CORP_ID, corporateId);
			inputs.put(ApplicationConstants.LOGIN_USERCODE, userCode);
			inputs.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, accountGroupDtlId);
			Map<String,Object> outputs = balanceSummaryService.singleAccountNonSummary(inputs);
			
			Map<String,Object> result = new HashMap<>();
			result.put("accountNo", outputs.get("accountNo"));
			result.put("accountName", outputs.get("accountName"));
			result.put("accountTypeCode", outputs.get("accountTypeCode"));
			result.put("accountTypeName", outputs.get("accountTypeName"));			
			result.put("accountCurrencyCode", outputs.get("accountCurrencyCode"));
			result.put("accountCurrencyName", outputs.get("accountCurrencyName"));
			result.put("availableBalance", outputs.get("availableBalance"));			
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public BigDecimal checkBalance(AccountModel account) throws ApplicationException, BusinessException {
		try {
			Map<String,Object> outputs = balanceSummaryService.doCheckBalance(account);
			return (BigDecimal) outputs.get("availableBalance");			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public void updateCreatedTransactionByUserCode(String userCode) throws ApplicationException {
		try {
			userTransactionMappingService.updateCreatedTransactionByUserCode(userCode);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public void updateExecutedTransactionByUserCode(String userCode) throws ApplicationException {
		try {
			userTransactionMappingService.updateExecutedTransactionByUserCode(userCode);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public List<Map<String, Object>> prepareDetailTransactionMapFromPendingTask(CorporateUserPendingTaskModel model, TransactionStatusModel trxStatus) throws Exception {
		List<Map<String, Object>> executedTransactionList = new LinkedList<>();

		AccountModel sourceAccount = accountRepo.findOne(model.getSourceAccount());
		
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("executedDate", trxStatus.getActivityDate());
		modelMap.put("systemReferenceNo", model.getId());
		modelMap.put("debitAccount", sourceAccount.getAccountNo());
		modelMap.put("debitAccountName", sourceAccount.getAccountName());
		modelMap.put("debitAccountCurrency", sourceAccount.getCurrency().getCode());
		modelMap.put("debitAccountCurrencyName", sourceAccount.getCurrency().getName());
		modelMap.put("transactionCurrency", model.getTransactionCurrency());
		modelMap.put("transactionAmount", model.getTransactionAmount());
		modelMap.put("creditAccount", ValueUtils.getValue(model.getBenAccount()));
		modelMap.put("creditAccountName", ValueUtils.getValue(model.getBenAccountName()));
		modelMap.put("creditAccountCurrency", ValueUtils.getValue(model.getBenAccountCurrencyCode()));
		
		modelMap.put("status", trxStatus.getStatus());
		modelMap.put("errorCode", ValueUtils.getValue(trxStatus.getErrorCode(), ApplicationConstants.EMPTY_STRING));
		
		//ini buat di translate
		Locale locale = LocaleContextHolder.getLocale();
		modelMap.put("errorDscp", message.getMessage(trxStatus.getErrorCode(), null, trxStatus.getErrorCode(), locale));
		
		executedTransactionList.add(modelMap);
		
		return executedTransactionList;
	}
	
	@Override
	public void save(String corporateId, String menuCode, String serviceCode, String referenceNo,
			String modelId, String transactionCurrency, BigDecimal totalChargeEquivalent, BigDecimal totalTransactionAmountEquivalent,
			BigDecimal totalDebitedAmountEquivalent, String isError) throws ApplicationException, BusinessException {
		try {
			GlobalTransactionModel model = new GlobalTransactionModel();
			model.setCorporateId(corporateId);
			
			IDMMenuModel menu = new IDMMenuModel();
			menu.setCode(menuCode);
			model.setMenu(menu);
			
			ServiceModel service = new ServiceModel();
			service.setCode(serviceCode);
			model.setService(service);
			
			model.setReferenceNo(referenceNo);
			model.setModelId(modelId);
			model.setTransactionCurrency(transactionCurrency);
			model.setTotalChargeEquivalent(totalChargeEquivalent);
			model.setTotalTransactionAmountEquivalent(totalTransactionAmountEquivalent);
			model.setTotalDebitedAmountEquivalent(totalDebitedAmountEquivalent);
			model.setIsError(isError);
			model.setCreatedDate(DateUtils.getCurrentTimestamp());
			globalTransactionRepo.save(model);
		} catch (Exception e) {
			logger.error("error save global transaction");
		}		
	}
}
