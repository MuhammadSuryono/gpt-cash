package com.gpt.product.gpcash.pendingdownload.services;

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
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class PendingDownloadSCImpl implements PendingDownloadSC {
	@Autowired
	private PendingDownloadService pendingDownloadService;
	
	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = ApplicationConstants.STR_MENUCODE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH })
	})
	@Output({
		@Variable(name = "result", type = List.class, required = false, subVariables= {
			@SubVariable(name = "id"),	
			@SubVariable(name = "createdDate"),
			@SubVariable(name = "fileType"),
			@SubVariable(name = ApplicationConstants.FILENAME),
			@SubVariable(name = "status"),
			@SubVariable(name = "statusDescription"),
			@SubVariable(name = "isReadyForDownload")
		})
	})	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingDownloadService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "fileFormat"),
		@Variable(name = ApplicationConstants.STR_MENUCODE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingDownloadService.submit(map);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void doGenerateReport(String parameter) throws ApplicationException, BusinessException {
		pendingDownloadService.doGenerateReport(parameter);
	}
}
