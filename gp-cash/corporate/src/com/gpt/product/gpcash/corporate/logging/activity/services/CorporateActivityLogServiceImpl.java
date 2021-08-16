package com.gpt.product.gpcash.corporate.logging.activity.services;

import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;
import javax.persistence.Tuple;

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
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporate.services.CorporateService;
import com.gpt.product.gpcash.corporate.logging.activity.model.CorporateActivityLogModel;
import com.gpt.product.gpcash.corporate.logging.activity.repository.CorporateActivityLogRepository;
import com.gpt.product.gpcash.corporate.logging.activity.valueobject.CorporateActivityLogVO;
import com.gpt.product.gpcash.corporate.logging.error.model.CorporateErrorLogModel;
import com.gpt.product.gpcash.corporate.logging.error.repository.CorporateErrorLogRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.repository.CorporateAdminPendingTaskRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityBean;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateActivityLogServiceImpl implements CorporateActivityLogService {
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CorporateActivityLogRepository activityLogRepo;
	
	@Autowired
	private CorporateRepository corporateRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private CorporateService corporateService;
	
	@Autowired
	private CorporateErrorLogRepository errorLogRepo;
	
	@Autowired
	private CorporateAdminPendingTaskRepository corporateAdminPendingTaskRepo;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private MessageSource message;
	
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void saveCorporateActivityLog(CorporateActivityLogVO vo) throws Exception {
		CorporateActivityLogModel log = new CorporateActivityLogModel();
		log.setActivityDate(vo.getActivityDate());
		log.setMenuCode(vo.getMenuCode());
		log.setMenuName(vo.getMenuName());
		log.setActionType(vo.getActionType());
		log.setActionBy(vo.getActionBy());
		log.setIsError(vo.isError() ? ApplicationConstants.YES : ApplicationConstants.NO);
		log.setErrorCode(vo.getErrorCode());
		log.setErrorDescription(vo.getErrorDescription());
		log.setReferenceNo(vo.getReferenceNo());
		log.setCorporateId(vo.getCorporateId());
		
		activityLogRepo.save(log);
		
		vo.setId(log.getId());
		if(vo.isError()) {
			CorporateErrorLogModel elog = new CorporateErrorLogModel();
			elog.setId(vo.getId());
			elog.setActivityDate(vo.getActivityDate());
			elog.setReferenceNo(vo.getReferenceNo());
			elog.setCorporateId(vo.getCorporateId());
			elog.setErrorTrace(vo.getErrorTrace());
			elog.setLogId(MDCHelper.getTraceId());
			
			errorLogRepo.persist(elog);
		}
	}
	
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Tuple> result = activityLogRepo.searchActivityLog(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<Tuple> list) {
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (Tuple tuple : list) {
			resultList.add(setModelToMap((CorporateActivityLogModel)tuple.get("act"), (String)tuple.get("name")));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(CorporateActivityLogModel activity, String userName) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", activity.getId());
		map.put("actionBy", userName);
		map.put("actionDate", activity.getActivityDate());
		map.put("actionType", activity.getActionType());
		map.put("isError",  ValueUtils.getValue(activity.getIsError()));
		map.put("errorCode", ValueUtils.getValue(activity.getErrorCode()));
		map.put("errorDescription", ValueUtils.getValue(activity.getErrorDescription()));
		map.put(ApplicationConstants.STR_MENUCODE, activity.getMenuCode());
		map.put("menuName", ValueUtils.getValue(activity.getMenuName()));
		map.put("referenceNo", ValueUtils.getValue(activity.getReferenceNo()));
		
		return map;
	}
	
	@Override
	public Map<String, Object> getNonFinancialActvity(Map<String, Object> map) throws ApplicationException, BusinessException{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			if(!ValueUtils.hasValue(referenceNo)) {
				String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
				String menuType = (String) map.get("menuType");
				map.put("activityLogMenuList", corporateService.findListOfStringNonFinancialMenu(corporateId, menuType));
				
				String actionByUserId = (String) map.get("actionByUserId");
				if(ValueUtils.hasValue(actionByUserId)) {
					String userCode = Helper.getCorporateUserCode(corporateId, actionByUserId);
					map.put("actionBy", userCode);
				}
			}
			
			String status = (String) map.get("status");
			if(ValueUtils.hasValue(status) && !ApplicationConstants.ALL_STS.equals(status)) {
				String isErrorFlag = ApplicationConstants.NO;
				if(status.equals(ApplicationConstants.FAILED_STS)) {
					isErrorFlag = ApplicationConstants.YES;
				}
				map.put("isError", isErrorFlag);
			}
			Page<Tuple> result = activityLogRepo.searchActivityLog(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMapForNonFinancial(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMapForNonFinancial(List<Tuple> list) {
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (Tuple tuple : list) {
			resultList.add(setModelToMapForNonFinancial((CorporateActivityLogModel)tuple.get("act"), (String)tuple.get("name")));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMapForNonFinancial(CorporateActivityLogModel activity, String userName) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", activity.getId());
		
		IDMUserModel idmUser = idmUserRepo.findOne(activity.getActionBy());
		map.put("actionByUserId", idmUser.getUserId());
		map.put("actionByUserName", idmUser.getName());
		
		map.put("actionDate", activity.getActivityDate());
		map.put("actionType", activity.getActionType());
		
		String status = ApplicationConstants.SUCCESS_STS;
		if(ApplicationConstants.YES.equals(activity.getIsError())) {
			status = ApplicationConstants.FAILED_STS;
		}
		map.put("status", status);
		
		map.put("errorCode", ValueUtils.getValue(activity.getErrorCode()));
		map.put("errorDescription", ValueUtils.getValue(activity.getErrorDescription()));
		map.put("activityLogMenuCode", activity.getMenuCode());
		map.put("activityLogMenuName", ValueUtils.getValue(activity.getMenuName()));
		map.put("referenceNo", ValueUtils.getValue(activity.getReferenceNo()));
		
		CorporateModel corporate = corporateRepo.findOne(activity.getCorporateId());
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());

		CorporateAdminPendingTaskModel pendingTask = 
				corporateAdminPendingTaskRepo.findByReferenceNo(activity.getReferenceNo());
		
		if(activity.getReferenceNo() != null && pendingTask != null) {
			map.put("pendingTaskId", pendingTask.getId());
			map.put("uniqueKeyDisplay", pendingTask.getUniqueKeyDisplay());
		} else {
			map.put("pendingTaskId", ApplicationConstants.EMPTY_STRING);
			map.put("uniqueKeyDisplay", ApplicationConstants.EMPTY_STRING);
		}
		
		
		return map;
	}

	@Override
	public Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		try {
			Locale locale = LocaleContextHolder.getLocale();
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			String userName = idmUserRepo.findOne(userCode).getName();
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			reportParams.put("requestReportUserName", userName);
			reportParams.put("corporateName", corporateRepo.findOne(corpId).getName());
			reportParams.put("address1", corporateRepo.findOne(corpId).getAddress1());
			reportParams.put("address2", corporateRepo.findOne(corpId).getAddress2());
			reportParams.put("address3", corporateRepo.findOne(corpId).getAddress3());
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo;
			bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			List<TransactionActivityBean> detailDSList = new ArrayList<>();
			
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			if(!ValueUtils.hasValue(referenceNo)) {
				String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
				String menuType = (String) map.get("menuType");
				map.put("activityLogMenuList", corporateService.findListOfStringNonFinancialMenu(corporateId, menuType));
				
				String actionByUserId = (String) map.get("actionByUserId");
				if(ValueUtils.hasValue(actionByUserId)) {
					String actionBy = Helper.getCorporateUserCode(corporateId, actionByUserId);
					map.put("actionBy", actionBy);
				}
			}
			
			String status = (String) map.get("status");
			if(ValueUtils.hasValue(status) && !ApplicationConstants.ALL_STS.equals(status)) {
				String isErrorFlag = ApplicationConstants.NO;
				if(status.equals(ApplicationConstants.FAILED_STS)) {
					isErrorFlag = ApplicationConstants.YES;
				}
				map.put("isError", isErrorFlag);
			}
			map.put("corporateId", corpId);
			Pageable pageInfo = new PageRequest(0,Integer.MAX_VALUE, new Sort("activityDate"));
			Page<Tuple> result = activityLogRepo.searchActivityLog(map, pageInfo);
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			resultList = setModelToMapForNonFinancial(result.getContent());
			
			if (resultList != null && !resultList.isEmpty()) {
				TransactionActivityBean trxActivity = null;
				int no = 1;
				for (Map<String, Object> resultMap : resultList) {
					
					trxActivity = new TransactionActivityBean();
					trxActivity.setNo(String.valueOf(no));
					trxActivity.setLatestActivity(sdfDateTime.format(resultMap.get("actionDate")));
					trxActivity.setReferenceNo(String.valueOf(resultMap.get("referenceNo")));
					trxActivity.setMenuName(String.valueOf(resultMap.get("activityLogMenuName")));
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
			
			String masterReportFile = reportFolder + File.separator + "CorporateAdmin" + File.separator + "NonFinancial" + File.separator + "download-transaction" + "-" + locale.getLanguage() + ".jasper";;
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
