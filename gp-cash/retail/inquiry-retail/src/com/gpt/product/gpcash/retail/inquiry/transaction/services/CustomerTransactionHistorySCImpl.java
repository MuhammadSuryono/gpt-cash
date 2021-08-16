package com.gpt.product.gpcash.retail.inquiry.transaction.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;

@Validate
@Service
public class CustomerTransactionHistorySCImpl implements CustomerTransactionHistorySC {

	@Autowired
	private CustomerTransactionHistoryService transactionHistoryService;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@EnableCustomerActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "openingBalance", type = BigDecimal.class),
		@Variable(name = "endingBalance", type = BigDecimal.class),		
		@Variable(name = "totalDebitAmount", type = BigDecimal.class),
		@Variable(name = "totalDebitTrx", type = Integer.class),
		@Variable(name = "totalCreditAmount", type = BigDecimal.class),
		@Variable(name = "totalCreditTrx", type = Integer.class),	
		@Variable(name = "transactions", type = List.class, subVariables = {
			@SubVariable(name = "postDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "effectiveDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "debitAmount", type = BigDecimal.class),
			@SubVariable(name = "creditAmount", type = BigDecimal.class),
			@SubVariable(name = "balance", required = false, type = BigDecimal.class),
			@SubVariable(name = "description", required = false)
		}),
		@Variable(name = "totalRecord", type = Integer.class), 
		@Variable(name = "totalPage", type = Integer.class)		
	})	
	@Override
	public Map<String, Object> periodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionHistoryService.periodicTransaction(map);
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_CSV, 
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_MT_940,
				ApplicationConstants.FILE_FORMAT_MT_942,
				ApplicationConstants.FILE_FORMAT_PDF,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = "isZip", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadPeriodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		String fileFormat = (String) map.get("fileFormat");
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			List<String> accountDtlIdList = new ArrayList<>();
			accountDtlIdList.add((String) map.get(ApplicationConstants.ACCOUNT_DTL_ID));
			map.put("accountDtlIdList", accountDtlIdList);
			return transactionHistoryService.downloadPeriodicTransactionMultiAccountForPDF(map);
		} else {
			return transactionHistoryService.downloadPeriodicTransaction(map);
		}
	}	
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "accountDtlIdList", type = List.class),
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_CSV, 
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_MT_940,
				ApplicationConstants.FILE_FORMAT_MT_942,
				ApplicationConstants.FILE_FORMAT_PDF,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = "fileSetting", options = {ApplicationConstants.FILE_SETTING_SEPARATED, ApplicationConstants.FILE_SETTING_CONSOLIDATED}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadPeriodicTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		String fileFormat = (String) map.get("fileFormat");
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return transactionHistoryService.downloadPeriodicTransactionMultiAccountForPDF(map);
		} else {
			return transactionHistoryService.downloadPeriodicTransactionMultiAccount(map);
		}
	}
	
	@EnableCustomerActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "openingBalance", type = BigDecimal.class),
		@Variable(name = "endingBalance", type = BigDecimal.class),		
		@Variable(name = "totalDebitAmount", type = BigDecimal.class),
		@Variable(name = "totalDebitTrx", type = Integer.class),		
		@Variable(name = "totalCreditAmount", type = BigDecimal.class),
		@Variable(name = "totalCreditTrx", type = Integer.class),		
		@Variable(name = "transactions", type = List.class, subVariables = {
			@SubVariable(name = "postDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "effectiveDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "debitAmount", type = BigDecimal.class),
			@SubVariable(name = "creditAmount", type = BigDecimal.class),
			@SubVariable(name = "balance", required = false, type = BigDecimal.class),
			@SubVariable(name = "description", required = false)
		}),
		@Variable(name = "totalRecord", type = Integer.class), 
		@Variable(name = "totalPage", type = Integer.class)		
	})	
	@Override
	public Map<String, Object> todayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Date today = new Date();
		//untuk testing ke host (jgn di hapus dl) >> perlu remark jg yg dr db di class DoCustomerTransactionHistory untuk test
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.DAY_OF_MONTH, 25);
//		cal.set(Calendar.MONTH, 4);
//		cal.set(Calendar.YEAR, 2017);
//		System.out.println("date ====== " + cal.getTime());
//		map.put("fromDate", cal.getTime());
//		map.put("toDate", cal.getTime());
		//---------------------------------------------------
		
		map.put("fromDate", today);
		map.put("toDate", today);
		return transactionHistoryService.periodicTransaction(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_CSV, 
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_MT_940,
				ApplicationConstants.FILE_FORMAT_MT_942,
				ApplicationConstants.FILE_FORMAT_PDF,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = "isZip", options = {ApplicationConstants.YES, ApplicationConstants.NO}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadTodayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Date today = new Date();
		map.put("fromDate", today);
		map.put("toDate", today);		
		
		String fileFormat = (String) map.get("fileFormat");
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			List<String> accountDtlIdList = new ArrayList<>();
			accountDtlIdList.add((String) map.get(ApplicationConstants.ACCOUNT_DTL_ID));
			map.put("accountDtlIdList", accountDtlIdList);
			return transactionHistoryService.downloadPeriodicTransactionMultiAccountForPDF(map);
		} else {
			return transactionHistoryService.downloadPeriodicTransaction(map);
		}
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "accountDtlIdList", type = List.class),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_CSV, 
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_MT_940,
				ApplicationConstants.FILE_FORMAT_MT_942,
				ApplicationConstants.FILE_FORMAT_PDF,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = "fileSetting", options = {ApplicationConstants.FILE_SETTING_SEPARATED, ApplicationConstants.FILE_SETTING_CONSOLIDATED}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadTodayTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		Date today = new Date();
		map.put("fromDate", today);
		map.put("toDate", today);		
		
		String fileFormat = (String) map.get("fileFormat");
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return transactionHistoryService.downloadPeriodicTransactionMultiAccountForPDF(map);
		} else {
			return transactionHistoryService.downloadPeriodicTransactionMultiAccount(map);
		}
		
	}
	
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.ACCOUNT_DTL_ID),
		@Variable(name = "limit", type = Integer.class),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "openingBalance", type = BigDecimal.class),
		@Variable(name = "endingBalance", type = BigDecimal.class),		
		@Variable(name = "totalDebitAmount", type = BigDecimal.class),
		@Variable(name = "totalDebitTrx", type = Integer.class),		
		@Variable(name = "totalCreditAmount", type = BigDecimal.class),
		@Variable(name = "totalCreditTrx", type = Integer.class),		
		@Variable(name = "transactions", type = List.class, subVariables = {
			@SubVariable(name = "postDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "effectiveDate", required = false, type = Date.class, format = Format.DATE),
			@SubVariable(name = "debitAmount", type = BigDecimal.class),
			@SubVariable(name = "creditAmount", type = BigDecimal.class),
			@SubVariable(name = "balance", required = false, type = BigDecimal.class),
			@SubVariable(name = "description", required = false)
		})	
	})	
	@Override
	public Map<String, Object> latestTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		return transactionHistoryService.latestTransaction(map);
	}	

	@Validate
	@Input({
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "accounts", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.ACCOUNT_DTL_ID),
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
	public Map<String, Object> searchCustomerAccountForInquiry(Map<String, Object> map) throws ApplicationException, BusinessException {
		List<String> casaAccountType = new ArrayList<>();
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_SAVING);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_CURRENT);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_TIME_DEPOSIT);
	    casaAccountType.add(ApplicationConstants.ACCOUNT_TYPE_LOAN);
	    
		return customerAccountService.findCASAAccountByCustomerAndAccountTypeForInquiryOnlyGetMap((String) map.get(ApplicationConstants.CUST_ID), casaAccountType);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeSOTRequestScheduler(String parameter) throws ApplicationException, BusinessException {
		transactionHistoryService.executeSOTRequestScheduler(parameter);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeSOTResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		transactionHistoryService.executeSOTResponseScheduler(parameter);
	}
}
