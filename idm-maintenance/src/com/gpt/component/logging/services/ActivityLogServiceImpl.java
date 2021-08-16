package com.gpt.component.logging.services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.logging.ActivityLogBean;
import com.gpt.component.logging.model.ActivityLogModel;
import com.gpt.component.logging.model.ErrorLogModel;
import com.gpt.component.logging.repository.ActivityLogRepository;
import com.gpt.component.logging.repository.ErrorLogRepository;
import com.gpt.component.logging.valueobject.ActivityLogVO;
import com.gpt.component.pendingtask.model.PendingTaskModel;
import com.gpt.component.pendingtask.repository.PendingTaskRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
public class ActivityLogServiceImpl implements ActivityLogService{
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IDMMenuRepository menuRepo;
	
	@Autowired
	private PendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private ActivityLogRepository activityLogRepo;

	@Autowired
	private ErrorLogRepository errorLogRepo;
	
	@Autowired
	private MessageSource message;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void saveActivityLog(ActivityLogVO vo) throws Exception {
		ActivityLogModel log = new ActivityLogModel();
		log.setActivityDate(vo.getActivityDate());
		log.setMenuCode(vo.getMenuCode());
		log.setMenuName(vo.getMenuName());
		log.setActionType(vo.getActionType());
		log.setActionBy(vo.getActionBy());
		log.setIsError(vo.isError() ? ApplicationConstants.YES : ApplicationConstants.NO);
		log.setErrorCode(vo.getErrorCode());
		log.setErrorDescription(vo.getErrorDescription());
		log.setReferenceNo(vo.getReferenceNo());
		
		activityLogRepo.save(log);
		
		vo.setId(log.getId());

		if(vo.isError()) {
			ErrorLogModel elog = new ErrorLogModel();
			elog.setId(vo.getId());
			elog.setActivityDate(vo.getActivityDate());
			elog.setReferenceNo(vo.getReferenceNo());
			elog.setErrorTrace(vo.getErrorTrace());
			elog.setLogId(MDCHelper.getTraceId());
			
			errorLogRepo.persist(elog);
		}
		
	}

	@Override
	public Map<String, Object> getMenuForActivityLog(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		List<IDMMenuModel> menuList = menuRepo.findByApplicationCodeForActivityLog((String) map.get(ApplicationConstants.APP_CODE));
		
		resultMap.put("result", setModelMenuToMap(menuList));
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelMenuToMap(List<IDMMenuModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMMenuModel model : list) {
			resultList.add(setModelMenuToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelMenuToMap(IDMMenuModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_MENUCODE, model.getCode());
		map.put("menuName", model.getName());
		
		return map;
	}
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String status = (String) map.get("status");
			if(ValueUtils.hasValue(status) && !ApplicationConstants.ALL_STS.equals(status)) {
				String isErrorFlag = ApplicationConstants.NO;
				if(status.equals(ApplicationConstants.FAILED_STS)) {
					isErrorFlag = ApplicationConstants.YES;
				}
				map.put("isError", isErrorFlag);
			}
			
			Page<ActivityLogModel> result = activityLogRepo.searchActivityLog(map,
					PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<ActivityLogModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ActivityLogModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(ActivityLogModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model.getId());
		
		IDMUserModel idmUser = idmUserRepo.findOne(model.getActionBy());
		map.put("actionBy", ValueUtils.getValue(model.getActionBy()));
		map.put("actionByUserName", idmUser.getName());
		
		map.put("actionDate", model.getActivityDate());
		map.put("actionType", model.getActionType());
		
		String status = ApplicationConstants.SUCCESS_STS;
		if(ApplicationConstants.YES.equals(model.getIsError())) {
			status = ApplicationConstants.FAILED_STS;
		}
		map.put("status", status);
		
		map.put("isError",  ValueUtils.getValue(model.getIsError()));
		map.put("errorCode", ValueUtils.getValue(model.getErrorCode()));
		map.put("errorDescription", ValueUtils.getValue(model.getErrorDescription()));
		map.put(ApplicationConstants.STR_MENUCODE, model.getMenuCode());
		map.put("menuName", ValueUtils.getValue(model.getMenuName()));
		map.put("referenceNo", ValueUtils.getValue(model.getReferenceNo()));
		
		PendingTaskModel pendingTask = 
				pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
		
		if(model.getReferenceNo() != null && pendingTask != null) {
			map.put("pendingTaskId", pendingTask.getId());
			map.put("uniqueKeyDisplay", pendingTask.getUniqueKeyDisplay());
		} else {
			map.put("pendingTaskId", ApplicationConstants.EMPTY_STRING);
			map.put("uniqueKeyDisplay", ApplicationConstants.EMPTY_STRING);
		}
		
		return map;
	}
	
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<ActivityLogModel> result = activityLogRepo.searchActivityLog(map,
					PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}

	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Locale locale = LocaleContextHolder.getLocale();
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERID);
			String userName = idmUserRepo.findOne(userCode).getName();
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			reportParams.put("requestReportUserName", userCode.concat(" - ").concat(userName));
			
			List<ActivityLogBean> detailDSList = new ArrayList<>();
			
			String status = (String) map.get("status");
			if(ValueUtils.hasValue(status) && !ApplicationConstants.ALL_STS.equals(status)) {
				String isErrorFlag = ApplicationConstants.NO;
				if(status.equals(ApplicationConstants.FAILED_STS)) {
					isErrorFlag = ApplicationConstants.YES;
				}
				map.put("isError", isErrorFlag);
			}
			
			Pageable pageInfo = new PageRequest(0,Integer.MAX_VALUE, new Sort("activityDate"));
			Page<ActivityLogModel> result = activityLogRepo.searchActivityLog(map, pageInfo);
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			resultList = setModelToMap(result.getContent());
			
			if (resultList != null && !resultList.isEmpty()) {
				ActivityLogBean trxActivity = null;
				int no = 1;
				for (Map<String, Object> resultMap : resultList) {
					
					trxActivity = new ActivityLogBean();
					trxActivity.setNo(String.valueOf(no));
					trxActivity.setLatestActivity(sdfDateTime.format(resultMap.get("actionDate")));
					trxActivity.setReferenceNo(String.valueOf(resultMap.get("referenceNo")));
					trxActivity.setMenuName(String.valueOf(resultMap.get("menuName")));
					trxActivity.setActivityType(String.valueOf(resultMap.get("actionType")));
					trxActivity.setActivityBy(String.valueOf(resultMap.get("actionByUserName")));
					trxActivity.setStatus(message.getMessage(String.valueOf(resultMap.get("status")), null, locale));
					detailDSList.add(trxActivity);
					no++;
				}
			}
			
			JRBeanCollectionDataSource detailDataSource= new JRBeanCollectionDataSource(detailDSList);
			reportParams.put("transactionDataSource", detailDataSource);
			
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".").concat("pdf");
			
			String masterReportFile = reportFolder + File.separator + "BankReport" + File.separator + "BankActivity" + File.separator + "download-bank-activity" + "-" + locale.getLanguage() + ".jasper";;
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, reportParams, new JREmptyDataSource());
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
			Path destinationFilePath = Paths.get(destinationFile);
			
			//write files
			Files.write(destinationFilePath, bytes);
			
			Map<String, Object> returnMap = new HashMap<>();
			returnMap.put(ApplicationConstants.FILENAME, destinationFile);
			
			return returnMap;
			
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
