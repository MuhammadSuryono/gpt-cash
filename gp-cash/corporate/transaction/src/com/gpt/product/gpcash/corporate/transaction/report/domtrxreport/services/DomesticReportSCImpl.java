package com.gpt.product.gpcash.corporate.transaction.report.domtrxreport.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.pendingdownload.services.PendingDownloadSC;

@Validate
@Service
public class DomesticReportSCImpl implements DomesticReportSC{
	@Autowired
	private DomesticReportService domesticReportService;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private PendingDownloadSC pendingDownloadSC;
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.CORP_ID, format = Format.UPPER_CASE),		
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
		return corporateAccountGroupService.searchCorporateAccountGroupDetailForDebitOnlyGetMapForBank((String)map.get(ApplicationConstants.CORP_ID));
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = ApplicationConstants.CORP_ID),
			@SubVariable(name = "corporateName"),
		})
	})	
	@Override
	public Map<String, Object> searchCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		return corporateService.searchCorporates();
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = "result", type = List.class, subVariables = {
			@SubVariable(name = "srvcCd"),
			@SubVariable(name = "srvcNm"),
		})
	})	
	@Override
	public Map<String, Object> searchDomServiceList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return domesticReportService.getDomServiceList();
	}
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingDownloadSC.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_CSV,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_REQUEST_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingDownloadSC.submit(map);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = "downloadId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException {
		return domesticReportService.downloadReport(map, (String) map.get(ApplicationConstants.LOGIN_USERID));
	}

	@Validate
	@Input({
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "downloadId"),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_TXT,
				ApplicationConstants.FILE_FORMAT_CSV,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public void doGenerateReport(Map<String, Object> map) throws ApplicationException, BusinessException {
		domesticReportService.doGenerateReport(map, (String) map.get(ApplicationConstants.LOGIN_USERID));
		
	}

}
