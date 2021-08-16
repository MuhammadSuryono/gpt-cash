package com.gpt.product.gpcash.pendingdownload.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper.SpringBeanInvokerData;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.menumapping.repository.MenuMappingRepository;
import com.gpt.product.gpcash.pendingdownload.PendingDownloadStatus;
import com.gpt.product.gpcash.pendingdownload.model.PendingDownloadModel;
import com.gpt.product.gpcash.pendingdownload.repository.PendingDownloadRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class PendingDownloadServiceImpl implements PendingDownloadService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PendingDownloadRepository pendingDownloadRepo;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private MenuMappingRepository menuMappingRepo;
	
	@Autowired
	private ISpringBeanInvoker invoker;
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			String menuCode = (String) map.get(ApplicationConstants.STR_MENUCODE);
			Page<PendingDownloadModel> result = pendingDownloadRepo.searchPendingDownloadByUser(
					loginUserId, menuCode, PagingUtils.createPageRequest(map));

			resultMap.put("result", setPendingDownloadModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;		
	}
	
	private List<Map<String, Object>> setPendingDownloadModelToMap(List<PendingDownloadModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingDownloadModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", model.getId());
			map.put("fileType", model.getFileFormat());
			map.put(ApplicationConstants.FILENAME, model.getFileName());
			map.put("status", model.getStatus());
			
			Locale locale = LocaleContextHolder.getLocale();
			map.put("createdDate", model.getCreatedDate());
			map.put("statusDescription", message.getMessage(model.getStatus(), null, model.getStatus(), locale));
			map.put("isReadyForDownload", model.getIsReadyForDownload());
			
			resultList.add(map);
		}

		return resultList;
	}
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {			
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			String menuCode = (String) map.get(ApplicationConstants.STR_MENUCODE);
			String fileFormat = (String) map.get("fileFormat");
			String downloadRefNo = Helper.generateReportReferenceNo();
			String downloadFileName = downloadRefNo.concat(".").concat(getDownloadFileExtention(fileFormat));
			
			PendingDownloadModel pendingDownload = new PendingDownloadModel();
			pendingDownload.setFileName(downloadFileName);
			pendingDownload.setFileFormat(fileFormat);
			pendingDownload.setMenuCode(menuCode);
			pendingDownload.setStatus(PendingDownloadStatus.RPT0100001.name()); //new request
			pendingDownload.setIsReadyForDownload(ApplicationConstants.NO);
			pendingDownload.setCreatedBy(loginUserId);
			pendingDownload.setCreatedDate(DateUtils.getCurrentTimestamp());
			
			String jsonObj = objectMapper.writeValueAsString(map);
			pendingDownload.setValues(jsonObj);
			pendingDownloadRepo.save(pendingDownload);
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp());
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, downloadRefNo);
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200010");
			resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void updateToInProgress(String pendingDownloadId) throws ApplicationException {
		try {
			PendingDownloadModel pendingDownload = pendingDownloadRepo.findOne(pendingDownloadId);
			pendingDownload.setStatus(PendingDownloadStatus.RPT0100002.name()); //in progress
			pendingDownload.setUpdatedDate(DateUtils.getCurrentTimestamp());
			pendingDownload.setUpdatedBy(ApplicationConstants.CREATED_BY_SYSTEM);
			pendingDownloadRepo.save(pendingDownload);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateToComplete(String pendingDownloadId, String fullFilePath) throws ApplicationException {
		try {
			PendingDownloadModel pendingDownload = pendingDownloadRepo.findOne(pendingDownloadId);
			pendingDownload.setStatus(PendingDownloadStatus.RPT0100003.name()); //complete
			pendingDownload.setFullFilePath(fullFilePath);
			pendingDownload.setIsReadyForDownload(ApplicationConstants.YES);
			pendingDownload.setUpdatedDate(DateUtils.getCurrentTimestamp());
			pendingDownload.setUpdatedBy(ApplicationConstants.CREATED_BY_SYSTEM);
			pendingDownloadRepo.save(pendingDownload);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void updateToNewRequest(String pendingDownloadId) throws ApplicationException {
		try {
			PendingDownloadModel pendingDownload = pendingDownloadRepo.findOne(pendingDownloadId);
			pendingDownload.setStatus(PendingDownloadStatus.RPT0100001.name()); //new
			pendingDownload.setUpdatedDate(DateUtils.getCurrentTimestamp());
			pendingDownload.setUpdatedBy(ApplicationConstants.CREATED_BY_SYSTEM);
			pendingDownloadRepo.save(pendingDownload);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void doGenerateReport(String parameter) throws ApplicationException, BusinessException {
		try {
			List<PendingDownloadModel> pendingDownloads = pendingDownloadRepo.searchAllNew(PendingDownloadStatus.RPT0100001.name());
			
			List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
			for(PendingDownloadModel pendingDownload : pendingDownloads) {
				
				if(logger.isDebugEnabled()) {
					logger.debug("Process report with downloadId : " + pendingDownload.getId());
				}
				
				String service = menuMappingRepo.findOne(pendingDownload.getMenuCode()).getMenuService();
				HashMap<String,Object> map = new ObjectMapper().readValue(pendingDownload.getValuesStr(), HashMap.class);
				map.put("downloadId", pendingDownload.getId());
				map.put(ApplicationConstants.FILENAME, pendingDownload.getFileName());
				
				if(ValueUtils.hasValue(map.get("fromDate"))) {
					Date fromDate = new Date((Long) map.get("fromDate"));
					map.put("fromDate", fromDate);
				}
				
				if(ValueUtils.hasValue(map.get("toDate"))) {
					Date toDate = new Date((Long) map.get("toDate"));
					map.put("toDate", toDate);
				}
				
				jobs.add(new SpringBeanInvokerData<>(pendingDownload.getId(), service, "doGenerateReport", new Object[] {map}, jobTimeout));
			}
			
			SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
		} catch (Exception e) {
			logger.error("Failed to doGenerateReport in PendingDownload " + e.getMessage(),e);
			throw new ApplicationException(e); 
		}
	}
	
	@Override
	public Map<String, Object> downloadReport(String pendingDownloadId, String downloadBy) throws ApplicationException {
		try {
			PendingDownloadModel pendingDownload = pendingDownloadRepo.findOne(pendingDownloadId);
			pendingDownload.setLastDownloadedBy(downloadBy);
			pendingDownload.setLastDownloadedDate(DateUtils.getCurrentTimestamp());
			pendingDownload.setUpdatedDate(DateUtils.getCurrentTimestamp());
			pendingDownload.setUpdatedBy(downloadBy);
			pendingDownloadRepo.save(pendingDownload);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, pendingDownload.getFullFilePath());
			
			return result;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private String getDownloadFileExtention(String fileFormat) {
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_CSV)) {
			return "csv";
		}
		
		return "txt";
	}
}
