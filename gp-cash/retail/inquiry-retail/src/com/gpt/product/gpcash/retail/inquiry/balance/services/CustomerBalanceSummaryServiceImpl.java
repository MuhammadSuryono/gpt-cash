package com.gpt.product.gpcash.retail.inquiry.balance.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.AccountProductTypeModel;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerBalanceSummaryServiceImpl implements CustomerBalanceSummaryService {

	@Autowired
	private EAIEngine eaiAdapter;

	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;
	
	@Autowired
	private MessageSource message;	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> singleAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String custId = (String)map.get(ApplicationConstants.CUST_ID);
			String accountId = (String)map.get(ApplicationConstants.ACCOUNT_DTL_ID);
						
			CustomerAccountModel ca = customerUtilsRepo.isCustomerAccountIdValid(custId, accountId);
			
			AccountModel accountModel = ca.getAccount();

			Map<String, Object> account = new HashMap<>(9, 1);
			account.put("accountNo", accountModel.getAccountNo());
			account.put("accountName", accountModel.getAccountName());

			AccountTypeModel accountType = accountModel.getAccountType();
			account.put("accountTypeCode", accountType.getCode());
			account.put("accountTypeName", accountType.getName());

			CurrencyModel accountCurrency = accountModel.getCurrency();
			account.put("accountCurrencyCode", accountCurrency.getCode());
			account.put("accountCurrencyName", accountCurrency.getName());

			BranchModel branch = accountModel.getBranch();

			if (branch != null) {
				account.put("accountBranchCode", branch.getCode());
				account.put("accountBranchName", branch.getName());
			}

			String accountStatus = ApplicationConstants.ACCOUNT_SYNC_ACTIVE;
			if(accountModel.getInactiveStatus().equals(ApplicationConstants.YES)) {
				accountStatus = ApplicationConstants.ACCOUNT_SYNC_INACTIVE;
			}
			account.put("accountStatus", accountStatus);

			List<Map<String, Object>> accountList = new ArrayList<>();
			accountList.add(account);

			Map<String, Object> outputs = doBalanceInqury(accountList);

			return constructSummaryResult((List<Map<String, Object>>) outputs.get("accountList"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> multiAccountByBranch(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String accountBranchCode = (String) map.get("accountBranchCode");
			String custId = (String)map.get(ApplicationConstants.CUST_ID);
						
			List<CustomerAccountModel> accountModels = null;

			if (ValueUtils.hasValue(accountBranchCode)) {
				accountModels = customerAccountService.findCASAAccountByCustomerAndBranch(custId, accountBranchCode);
			} else {
				accountModels = customerAccountService.findCASAAccountByCustomer(custId);
			}

			if (ValueUtils.hasValue(accountModels)) {
				List<Map<String, Object>> accountList = new ArrayList<>();

				for (CustomerAccountModel customerAccount : accountModels) {
					Map<String, Object> account = new HashMap<>(9, 1);

					AccountModel accountModel = customerAccount.getAccount();
					account.put("accountNo", accountModel.getAccountNo());
					account.put("accountName", accountModel.getAccountName());

					AccountTypeModel accountType = accountModel.getAccountType();
					account.put("accountTypeCode", accountType.getCode());
					account.put("accountTypeName", accountType.getName());

					CurrencyModel accountCurrency = accountModel.getCurrency();
					account.put("accountCurrencyCode", accountCurrency.getCode());
					account.put("accountCurrencyName", accountCurrency.getName());

					BranchModel branch = accountModel.getBranch();

					if (branch != null) {
						account.put("accountBranchCode", branch.getCode());
						account.put("accountBranchName", branch.getName());
					}

					String accountStatus = ApplicationConstants.ACCOUNT_SYNC_ACTIVE;
					if(accountModel.getInactiveStatus().equals(ApplicationConstants.YES)) {
						accountStatus = ApplicationConstants.ACCOUNT_SYNC_INACTIVE;
					}
					account.put("accountStatus", accountStatus);

					accountList.add(account);
				}

				Map<String, Object> outputs = doBalanceInqury(accountList);

				return constructSummaryResult((List<Map<String, Object>>) outputs.get("accountList"));
			} else {
				Map<String, Object> result = new HashMap<>();
				result.put("casaInfo", new HashMap<>());
				result.put("loanInfo", new HashMap<>());
				result.put("tdInfo", new HashMap<>());
				return result;
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> multiAccountByAccountType(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String accountTypeCode = (String) map.get("accountTypeCode");
			String custId = (String) map.get(ApplicationConstants.CUST_ID);

			List<CustomerAccountModel> accountModels = null;
			
			if (ValueUtils.hasValue(accountTypeCode)) {
				List<String> accountTypeList = new ArrayList<>();
				accountTypeList.add(accountTypeCode);
				accountModels = customerAccountService.findCASAAccountByCustomerAndAccountType(custId, accountTypeList);
			} else {
				accountModels = customerAccountService.findCASAAccountByCustomer(custId);
			}

			if (ValueUtils.hasValue(accountModels)) {
				List<Map<String, Object>> accountList = new ArrayList<>();

				for (CustomerAccountModel customerAccount : accountModels) {
					Map<String, Object> account = new HashMap<>(9, 1);

					AccountModel accountModel = customerAccount.getAccount();
					account.put("accountNo", accountModel.getAccountNo());
					account.put("accountName", accountModel.getAccountName());

					AccountTypeModel accountType = accountModel.getAccountType();
					account.put("accountTypeCode", accountType.getCode());
					account.put("accountTypeName", accountType.getName());

					CurrencyModel accountCurrency = accountModel.getCurrency();
					account.put("accountCurrencyCode", accountCurrency.getCode());
					account.put("accountCurrencyName", accountCurrency.getName());

					BranchModel branch = accountModel.getBranch();

					if (branch != null) {
						account.put("accountBranchCode", branch.getCode());
						account.put("accountBranchName", branch.getName());
					}

					String accountStatus = ApplicationConstants.ACCOUNT_SYNC_ACTIVE;
					if(accountModel.getInactiveStatus().equals(ApplicationConstants.YES)) {
						accountStatus = ApplicationConstants.ACCOUNT_SYNC_INACTIVE;
					}
					account.put("accountStatus", accountStatus);
					
					accountList.add(account);
				}

				Map<String, Object> outputs = doBalanceInqury(accountList);

				return constructSummaryResult((List<Map<String, Object>>) outputs.get("accountList"));
			} else {
				Map<String, Object> result = new HashMap<>();
				result.put("casaInfo", new HashMap<>());
				result.put("loanInfo", new HashMap<>());
				result.put("tdInfo", new HashMap<>());
				return result;
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private Map<String, Object> doBalanceInqury(List<Map<String, Object>> accountList) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("accountList", accountList);

		return eaiAdapter.invokeService(EAIConstants.BALANCE_SUMMARY, inputs);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> doCheckBalance(AccountModel account) throws Exception {
		try {
			Map<String, Object> inputs = new HashMap<>();
			
			List<Map<String, Object>> accountList = new ArrayList<>();
			Map<String, Object> accountMap = new HashMap<>();
			accountMap.put("accountNo", account.getAccountNo());
			accountMap.put("accountTypeCode", account.getAccountType().getCode());
			accountList.add(accountMap);
			
			inputs.put("accountList", accountList);
		
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.BALANCE_SUMMARY, inputs);
			return (Map<String, Object>)((List<Map<String, Object>>)outputs.get("accountList")).get(0);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> constructSummaryResult(List<Map<String, Object>> accountList) {
		Map<String, Object> result = new HashMap<>();
		
		Map<String, Object> casaInfo = new HashMap<>();
		Map<String, Object> loanInfo = new HashMap<>();
		Map<String, Object> tdInfo = new HashMap<>();
		
		Map<String, Object> totalDepositInfo = new HashMap<>();
		Map<String, Object> totalBorrowingInfo = new HashMap<>();
		Map<String, String> currencyInfo = new HashMap<>();
		
		for (Map<String, Object> account : accountList) {
			if ("E".equals((String)account.get("status")))
				continue;

			String accountCurrencyCode = (String)account.get("accountCurrencyCode");			
			if (currencyInfo.get(accountCurrencyCode)==null)
				currencyInfo.put(accountCurrencyCode, accountCurrencyCode);
			
			String accountType = (String) account.get("accountTypeCode");
			if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_CURRENT)
					|| accountType.equals(ApplicationConstants.ACCOUNT_TYPE_SAVING)) {

				BigDecimal availableBalance = (BigDecimal) account.get("availableBalance");

				Map<String, Object> accountInfo = new HashMap<>();
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));				
				accountInfo.put("ledgerBalance", account.get("ledgerBalance"));
				accountInfo.put("availableBalance", availableBalance);
				accountInfo.put("creditLineAmount", account.get("creditLineAmount"));
				accountInfo.put("holdAmountInfos", account.get("holdAmountInfos"));

				Map<String, Object> currencyCasaInfo = (Map<String, Object>)casaInfo.get(accountCurrencyCode);
				if (currencyCasaInfo==null) {
					List<Map<String,Object>> accounts = new ArrayList<>();
					accounts.add(accountInfo);

					currencyCasaInfo = new HashMap<>();					
					currencyCasaInfo.put("totalAvailableBalance", availableBalance);
					currencyCasaInfo.put("accounts", accounts);
					
					casaInfo.put(accountCurrencyCode, currencyCasaInfo);
				} else {
					BigDecimal totalAvailableBalanceCASA = (BigDecimal)currencyCasaInfo.get("totalAvailableBalance");
					totalAvailableBalanceCASA = totalAvailableBalanceCASA.add(availableBalance);
					
					List<Map<String,Object>> accounts = (List<Map<String,Object>>)currencyCasaInfo.get("accounts");
					accounts.add(accountInfo);
					
					currencyCasaInfo.put("totalAvailableBalance", totalAvailableBalanceCASA);
					currencyCasaInfo.put("accounts", accounts);
				}
				
				BigDecimal currencyTotalDeposit = (BigDecimal)totalDepositInfo.get(accountCurrencyCode);
				if (currencyTotalDeposit==null) {
					totalDepositInfo.put(accountCurrencyCode, availableBalance);
				} else {
					currencyTotalDeposit = currencyTotalDeposit.add(availableBalance);					
					totalDepositInfo.put(accountCurrencyCode, currencyTotalDeposit);
				}
				
			} else if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_LOAN)) {
				BigDecimal loanAmount = (BigDecimal) account.get("loanAmount");

				Map<String, Object> accountInfo = new HashMap<>();
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));
				accountInfo.put("noteNumber", account.get("noteNumber"));
				accountInfo.put("loanAmount", loanAmount);
				accountInfo.put("outstandingAmount", account.get("outstandingAmount"));
				accountInfo.put("unusedAmount", account.get("unusedAmount"));
				accountInfo.put("interestRate", account.get("interestRate"));
				accountInfo.put("startDate", account.get("startDate"));
				accountInfo.put("endDate", account.get("endDate"));
				accountInfo.put("overdueLoan", account.get("overdueLoan"));
				accountInfo.put("overdueInterest", account.get("overdueInterest"));
				accountInfo.put("totalOverdue", account.get("totalOverdue"));
				accountInfo.put("productType", account.get("productType"));
								
				Map<String, Object> currencyLoanInfo = (Map<String, Object>)loanInfo.get(accountCurrencyCode);
				if (currencyLoanInfo==null) {
					List<Map<String,Object>> accounts = new ArrayList<>();
					accounts.add(accountInfo);

					currencyLoanInfo = new HashMap<>();					
					currencyLoanInfo.put("totalLoanAmount", loanAmount);
					currencyLoanInfo.put("accounts", accounts);
					
					loanInfo.put(accountCurrencyCode, currencyLoanInfo);					
				} else {
					BigDecimal totalLoanAmount = (BigDecimal)currencyLoanInfo.get("totalLoanAmount");
					totalLoanAmount = totalLoanAmount.add(loanAmount);
					
					List<Map<String,Object>> accounts = (List<Map<String,Object>>)currencyLoanInfo.get("accounts");
					accounts.add(accountInfo);
					
					currencyLoanInfo.put("totalLoanAmount", totalLoanAmount);
					currencyLoanInfo.put("accounts", accounts);
				}
				
				BigDecimal currencyTotalBorrowings = (BigDecimal)totalDepositInfo.get(accountCurrencyCode);
				if (currencyTotalBorrowings==null) {
					totalBorrowingInfo.put(accountCurrencyCode, loanAmount);
				} else {
					currencyTotalBorrowings = currencyTotalBorrowings.add(loanAmount);					
					totalBorrowingInfo.put(accountCurrencyCode, currencyTotalBorrowings);
				}
				
			} else if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT)) {
				BigDecimal principalAmount = (BigDecimal) account.get("principalAmount");

				Map<String, Object> accountInfo = new HashMap<>();
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));				
				accountInfo.put("certificateNo", account.get("certificateNo"));
				accountInfo.put("certificateStatus", account.get("certificateStatus"));
				accountInfo.put("principalAmount", principalAmount);
				accountInfo.put("interestRate", account.get("interestRate"));
				accountInfo.put("originalDate", account.get("originalDate"));
				accountInfo.put("maturityDate", account.get("maturityDate"));
				accountInfo.put("term", account.get("term"));
				accountInfo.put("sourceAccount", account.get("sourceAccount"));
				accountInfo.put("interestAccount", account.get("interestAccount"));
				
				String renewalCode = (String)account.get("renewalCode");
				accountInfo.put("renewalCode", renewalCode.equals("1")?"ARO":"NON ARO");
				
				String maturityInstruction = (String)account.get("maturityInstruction");
				if (maturityInstruction.equals("1")) // Interest add to Principle		
					maturityInstruction = "GPT-0100205"; 
				else if (maturityInstruction.equals("2")) // Interest add to Source Account
					maturityInstruction = "GPT-0100206"; 

				accountInfo.put("maturityInstruction", message.getMessage(maturityInstruction, null, maturityInstruction, LocaleContextHolder.getLocale()));
				
				Map<String, Object> currencyTdInfo = (Map<String, Object>)tdInfo.get(accountCurrencyCode);
				if (currencyTdInfo==null) {
					List<Map<String,Object>> accounts = new ArrayList<>();
					accounts.add(accountInfo);

					currencyTdInfo = new HashMap<>();					
					currencyTdInfo.put("totalPrincipalAmount", principalAmount);
					currencyTdInfo.put("accounts", accounts);
					
					tdInfo.put(accountCurrencyCode, currencyTdInfo);					
				} else {
					BigDecimal totalPrincipalAmount = (BigDecimal)currencyTdInfo.get("totalPrincipalAmount");
					totalPrincipalAmount = totalPrincipalAmount.add(principalAmount);
					
					List<Map<String,Object>> accounts = (List<Map<String,Object>>)currencyTdInfo.get("accounts");
					accounts.add(accountInfo);
					
					currencyTdInfo.put("totalPrincipalAmount", totalPrincipalAmount);
					currencyTdInfo.put("accounts", accounts);
				}
				
				BigDecimal currencyTotalDeposit = (BigDecimal)totalDepositInfo.get(accountCurrencyCode);
				if (currencyTotalDeposit==null) {
					totalDepositInfo.put(accountCurrencyCode, principalAmount);
				} else {
					currencyTotalDeposit = currencyTotalDeposit.add(principalAmount);					
					totalDepositInfo.put(accountCurrencyCode, currencyTotalDeposit);
				}
				
			}
		}
		
		Map<String, Object> totalInfo = new HashMap<>();
		totalInfo.put("deposits", totalDepositInfo);
		totalInfo.put("borrowings", totalBorrowingInfo);
		
		result.put("currency", new ArrayList<>(currencyInfo.keySet()));
		result.put("casaInfo", casaInfo);
		result.put("loanInfo", loanInfo);
		result.put("tdInfo", tdInfo);		
		result.put("total", totalInfo);

		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> singleAccountNonSummary(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String custId = (String)map.get(ApplicationConstants.CUST_ID);
			String accountId = (String)map.get(ApplicationConstants.ACCOUNT_DTL_ID);
						
			CustomerAccountModel ca = customerUtilsRepo.isCustomerAccountIdValid(custId, accountId);
			
			AccountModel accountModel = ca.getAccount();

			Map<String, Object> account = new HashMap<>(9, 1);
			account.put("accountNo", accountModel.getAccountNo());
			account.put("accountName", accountModel.getAccountName());
			
			String accountStatus = ApplicationConstants.ACCOUNT_SYNC_ACTIVE;
			if(accountModel.getInactiveStatus().equals(ApplicationConstants.YES)) {
				accountStatus = ApplicationConstants.ACCOUNT_SYNC_INACTIVE;
			}
			account.put("accountStatus", accountStatus);			

			AccountTypeModel accountType = accountModel.getAccountType();
			account.put("accountTypeCode", accountType.getCode());
			account.put("accountTypeName", accountType.getName());

			CurrencyModel accountCurrency = accountModel.getCurrency();
			account.put("accountCurrencyCode", accountCurrency.getCode());
			account.put("accountCurrencyName", accountCurrency.getName());

			BranchModel branch = accountModel.getBranch();

			if (branch != null) {
				account.put("accountBranchCode", branch.getCode());
				account.put("accountBranchName", branch.getName());
			}
			
			AccountProductTypeModel productType = accountModel.getAccountProductType();
			if (productType != null) {
				account.put("accountProductCode", productType.getCode());
				account.put("accountProductName", productType.getName());
			}

			List<Map<String, Object>> accountList = new ArrayList<>();
			accountList.add(account);

			Map<String, Object> outputs = doBalanceInqury(accountList);
			
			return constructNonSummaryResult((List<Map<String, Object>>) outputs.get("accountList"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	private Map<String, Object> constructNonSummaryResult(List<Map<String, Object>> accountList) throws Exception {
		if (accountList.size()>0) {			
			Map<String, Object> account = (Map<String,Object>)accountList.get(0);
			
			if ("E".equals((String)account.get("status")))
				throw new BusinessException((String)account.get("errorMessage"));

			Map<String, Object> accountInfo = new HashMap<>();
			String accountType = (String) account.get("accountTypeCode");
			if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_CURRENT) || accountType.equals(ApplicationConstants.ACCOUNT_TYPE_SAVING)) {
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));				
				accountInfo.put("ledgerBalance", account.get("ledgerBalance"));
				accountInfo.put("availableBalance", account.get("availableBalance"));
				accountInfo.put("creditLineAmount", account.get("creditLineAmount"));
				accountInfo.put("holdAmountInfos", account.get("holdAmountInfos"));				
			} else if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_LOAN)) {
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));				
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));				
				accountInfo.put("noteNumber", account.get("noteNumber"));
				accountInfo.put("loanAmount", account.get("loanAmount"));
				accountInfo.put("outstandingAmount", account.get("outstandingAmount"));
				accountInfo.put("unusedAmount", account.get("outstandingAmount"));
				accountInfo.put("interestRate", account.get("interestRate"));
				accountInfo.put("startDate", account.get("startDate"));
				accountInfo.put("endDate", account.get("endDate"));
				accountInfo.put("overdueLoan", account.get("overdueLoan"));
				accountInfo.put("overdueInterest", account.get("overdueInterest"));
				accountInfo.put("totalOverdue", account.get("totalOverdue"));
				accountInfo.put("productType", account.get("productType"));				
			} else if (accountType.equals(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT)) {
				accountInfo.put("accountNo", account.get("accountNo"));
				accountInfo.put("accountName", account.get("accountName"));
				accountInfo.put("accountStatus", account.get("accountStatus"));
				accountInfo.put("accountTypeCode", account.get("accountTypeCode"));
				accountInfo.put("accountTypeName", account.get("accountTypeName"));
				accountInfo.put("accountCurrencyCode", account.get("accountCurrencyCode"));
				accountInfo.put("accountCurrencyName", account.get("accountCurrencyName"));
				accountInfo.put("accountBranchCode", account.get("accountBranchCode"));
				accountInfo.put("accountBranchName", account.get("accountBranchName"));				
				accountInfo.put("certificateNo", account.get("certificateNo"));
				accountInfo.put("certificateStatus", account.get("certificateStatus"));
				accountInfo.put("principalAmount", account.get("principalAmount"));
				accountInfo.put("interestRate", account.get("interestRate"));
				accountInfo.put("originalDate", account.get("originalDate"));
				accountInfo.put("maturityDate", account.get("maturityDate"));
				accountInfo.put("term", account.get("term"));
				accountInfo.put("sourceAccount", account.get("sourceAccount"));
				accountInfo.put("interestAccount", account.get("interestAccount"));
				
				String renewalCode = (String)account.get("renewalCode");
				accountInfo.put("renewalCode", renewalCode.equals("1")?"ARO":"NON ARO");
				
				String maturityInstruction = (String)account.get("maturityInstruction");
				if (maturityInstruction.equals("1")) // Interest add to Principle		
					maturityInstruction = "GPT-0100205"; 
				else if (maturityInstruction.equals("2")) // Interest add to Source Account
					maturityInstruction = "GPT-0100206"; 

				accountInfo.put("maturityInstruction", message.getMessage(maturityInstruction, null, maturityInstruction, LocaleContextHolder.getLocale()));
			}

			return accountInfo;
		}
		
		return null;
	}	

}
