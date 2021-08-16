package com.gpt.product.gpcash.corporate.transaction.report.trxsummary.services;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.pendingdownload.services.PendingDownloadSC;

@Validate
@Service
public class TrxSummaryReportSCImpl implements TrxSummaryReportSC {

	@Autowired
	private TrxSummaryReportService trxSummaryService;
	
	@Autowired
	private PendingDownloadSC pendingDownloadSC;
	
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
				ApplicationConstants.FILE_FORMAT_PDF,
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
	public Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException {
		return trxSummaryService.downloadReport(map, (String) map.get(ApplicationConstants.LOGIN_USERID));
	}
	
	@Validate
	@Input({
		@Variable(name = "fromDate", type = Date.class, format = Format.DATE),
		@Variable(name = "toDate", type = Date.class, format = Format.DATE),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "downloadId"),
		@Variable(name = "fileFormat", options = {
				ApplicationConstants.FILE_FORMAT_PDF,
				ApplicationConstants.FILE_FORMAT_EXCEL}),
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Override
	public void doGenerateReport(Map<String, Object> map) throws ApplicationException, BusinessException {
		trxSummaryService.doGenerateReport(map, (String) map.get(ApplicationConstants.LOGIN_USERID));
	}
}
