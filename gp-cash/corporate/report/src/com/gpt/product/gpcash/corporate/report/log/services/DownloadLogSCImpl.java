package com.gpt.product.gpcash.corporate.report.log.services;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class DownloadLogSCImpl implements DownloadLogSC {

	@Autowired
	private DownloadLogService downloadLogService;
	
	
	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_USERID),
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_DOWNLOAD}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode),
		@Variable(name = "logDate", type = Date.class, format = Format.DATE, required = false)
	})
	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException {
		return downloadLogService.downloadReport(map, (String) map.get(ApplicationConstants.LOGIN_USERID));
	}
	
}
