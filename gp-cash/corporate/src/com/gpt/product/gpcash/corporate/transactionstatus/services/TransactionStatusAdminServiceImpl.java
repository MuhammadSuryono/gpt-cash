package com.gpt.product.gpcash.corporate.transactionstatus.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.repository.CorporateAdminPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityBean;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository.TransactionStatusMappingRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionStatusAdminServiceImpl implements TransactionStatusAdminService{
	
	@Autowired
	private TransactionStatusRepository transactionStatusRepo;
	
	@Autowired
	private CorporateUserPendingTaskRepository corporateUserPendingTaskRepo;
	
	@Autowired
	private CorporateAdminPendingTaskRepository corporateAdminPendingTaskRepo;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateAdminPendingTaskService pendingTaskAdminService;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private TransactionStatusMappingRepository transactionStatusMappingRepo;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private MessageSource message;

	@Override
	public Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
//			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
//			CorporateUserModel user = corporateUtilsRepo.isCorporateUserValid(loginUserId);
//			map.put("userGroupId", user.getCorporateUserGroup().getId());

			return wfEngine.findPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchTransactionStatusForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String makerUserId = (String) map.get("makerUserId");
			if(ValueUtils.hasValue(makerUserId)) {
				String corporateId = (String) map.get(ApplicationConstants.CORP_ID); 
				String userCode = Helper.getCorporateUserCode(corporateId, makerUserId);
				map.put("makerUserCode", userCode);
			}
			
			return wfEngine.findPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchNonFinancialActivityForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			return wfEngine.findNonFinancialPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			LinkedList<Map<String, Object>> activitiesList = new LinkedList<>();
			String pendingTaskId = (String) map.get("pendingTaskId");
			
			CorporateUserPendingTaskModel wfHistoryPendingTask = corporateUserPendingTaskRepo.findById(pendingTaskId);
			
			//1. get data from transaction status table
			List<TransactionStatusModel> trxStatusList = transactionStatusRepo.
					findByPendingTaskId(pendingTaskId);
			for(TransactionStatusModel trxStatus : trxStatusList) {
				Map<String, Object> activity = new HashMap<>();
				CorporateUserPendingTaskModel trxStatusPendingTask = corporateUserPendingTaskRepo.findById(trxStatus.getPendingTaskId());
				IDMUserModel idmUser = idmRepo.isIDMUserValid(trxStatus.getUser());
				
				activity.put("activityDate", trxStatus.getActivityDate());
				activity.put("activity", trxStatus.getActionType());
				
				//agar ui dapat membedakan ada tombol view atau tidak
				if(trxStatus.getActionType().equals(TransactionActivityType.EXECUTE_TO_HOST)) {
					if(trxStatus.getEaiRefNo() != null) {
						activity.put("isExecute", ApplicationConstants.YES);
						activity.put("executedId", trxStatus.getId());
					}
					
					if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS) || trxStatus.getStatus().equals(TransactionStatus.EXECUTE_PARTIAL_SUCCESS)
							||(trxStatus.getStatus().equals(TransactionStatus.IN_PROGRESS_OFFLINE) && ApplicationConstants.YES.equals(wfHistoryPendingTask.getErrorTimeoutFlag()))) {
						
						IDMMenuModel menu = wfHistoryPendingTask.getMenu();
						if(ApplicationConstants.YES.equals(menu.getIsFinancialFlag())) {
							activity.put("isReceipt", ApplicationConstants.YES);
							activity.put("receiptId", trxStatus.getId());
						}
					}
				}
				
				activity.put("userId", idmUser.getUserId());
				activity.put("userName", idmUser.getName());
				activity.put("amountCcyCd", trxStatusPendingTask.getTransactionCurrency());
				activity.put("amount", trxStatusPendingTask.getTransactionAmount());
				activity.put("status", trxStatus.getStatus());
				
				//TODO harusnya referenceNo di table jika exist
				activity.put("referenceNo", trxStatusPendingTask.getReferenceNo());
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			//2. get data from wf history
			Map<String, Object> pendingTaskHistory = pendingTaskService.searchPendingTaskHistoryByPendingTaskId(map);
			List<Map<String, Object>> pendingTaskHistoryActivities =  (List<Map<String,Object>>) pendingTaskHistory.get("activities");
			for(int i=pendingTaskHistoryActivities.size()-1; i>=0 ; i--) {
				Map<String, Object> activity = pendingTaskHistoryActivities.get(i);
				activity.put("referenceNo", wfHistoryPendingTask.getReferenceNo());
				
				if(TransactionActivityType.CREATE.toString().equals(activity.get("activity"))) {
					activity.put("isCreate", ApplicationConstants.YES);
				}
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			
			resultMap.put("activities", activitiesList);
			
			//Validasi LOGIN_USERCODE di perlukan agar transaction status bank tidak ada cancel
			if(map.get(ApplicationConstants.LOGIN_USERCODE) != null) {
				if(TransactionStatus.PENDING_EXECUTE.toString().equals(wfHistoryPendingTask.getTrxStatus().name())) {
					//requested by Susi (12 October 2017), hanya user maker yg boleh cancel
					String loginUserCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);
					if(loginUserCode.equals(wfHistoryPendingTask.getCreatedBy())) {
						resultMap.put("isCancel", ApplicationConstants.YES);
						resultMap.put("cancelId", pendingTaskId);
					}
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> detailTransactionStatusForNonFinancial(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			LinkedList<Map<String, Object>> activitiesList = new LinkedList<>();
			String pendingTaskId = (String) map.get("pendingTaskId");
			
			CorporateAdminPendingTaskModel wfHistoryPendingTask = corporateAdminPendingTaskRepo.findById(pendingTaskId);
			
			//get data from wf history
			Map<String, Object> pendingTaskHistory = pendingTaskAdminService.searchPendingTaskHistoryByPendingTaskId(pendingTaskId);
			List<Map<String, Object>> pendingTaskHistoryActivities =  (List<Map<String,Object>>) pendingTaskHistory.get("activities");
			for(int i=pendingTaskHistoryActivities.size()-1; i>=0 ; i--) {
				Map<String, Object> activity = pendingTaskHistoryActivities.get(i);
				activity.put("referenceNo", wfHistoryPendingTask.getReferenceNo());
				
				if(TransactionActivityType.CREATE.toString().equals(activity.get("activity"))) {
					activity.put("isCreate", ApplicationConstants.YES);
				}
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			
			resultMap.put("activities", activitiesList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String executedMenuCode = (String) map.get("executedMenuCode");
			
			String service = ((TransactionStatusMappingModel) transactionStatusMappingRepo.findOne(executedMenuCode)).getMenuService();
			
			map.put(ApplicationConstants.VIEW_FROM_BANK, ApplicationConstants.YES);
			
			resultMap = Util.invokeSpringBean(appCtx, service, "detailExecutedTransaction", map);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void addTransactionStatus(String pendingTaskId, Timestamp activityDate, TransactionActivityType actionType, String user, TransactionStatus status, String eaiRefNo, boolean error, String errorCode) {
		
		TransactionStatusModel model = new TransactionStatusModel();
		
		model.setPendingTaskId(pendingTaskId);
		model.setActivityDate(activityDate);
		model.setActionType(actionType);
		model.setUser(user);
		model.setStatus(status);
		model.setEaiRefNo(eaiRefNo);
		model.setError(error);
		if (error && errorCode==null)
			errorCode = "GPT-GENERALERROR";
		model.setErrorCode(errorCode);
		
		transactionStatusRepo.save(model);
		
	}

	@Override
	public Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Locale locale = LocaleContextHolder.getLocale();
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			DecimalFormat df = new DecimalFormat(moneyFormat); 
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			String userName = idmRepo.getUserRepo().findOne(userCode).getName();
			
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			reportParams.put("requestReportUserName", userName);
			reportParams.put("corporateName", corporateUtilsRepo.getCorporateRepo().findOne(corpId).getName());
			reportParams.put("address1", corporateUtilsRepo.getCorporateRepo().findOne(corpId).getAddress1());
			reportParams.put("address2", corporateUtilsRepo.getCorporateRepo().findOne(corpId).getAddress2());
			reportParams.put("address3", corporateUtilsRepo.getCorporateRepo().findOne(corpId).getAddress3());
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo;
			bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			Map<String, Object> result = wfEngine.findPendingTasksForDownload(map);
			List<Map<String, Object>> resultList = new ArrayList<>();
			resultList = (List<Map<String, Object>>) result.get("result");
			List<TransactionActivityBean> detailDSList = new ArrayList<>();
			
			if (resultList != null && !resultList.isEmpty()) {
				TransactionActivityBean trxActivity = null;
				int no = 1;
				for (Map<String, Object> resultMap : resultList) {
					String menuCode = String.valueOf(resultMap.get("pendingTaskMenuCode"));
					
					trxActivity = new TransactionActivityBean();
					trxActivity.setNo(String.valueOf(no));
					trxActivity.setLatestActivity(sdfDateTime.format(resultMap.get("latestActivityDate")));
					trxActivity.setReferenceNo(String.valueOf(resultMap.get("referenceNo")));
					trxActivity.setMenuName(String.valueOf(resultMap.get("pendingTaskMenuName")));
					if (!menuCode.equals("MNU_GPCASH_F_FUND_BENEFICIARY")) {
						trxActivity.setCorpAccount(ValueUtils.getValue(resultMap.get("sourceAccount")) + " - " + ValueUtils.getValue(resultMap.get("sourceAccountName")) + " ("+ValueUtils.getValue(resultMap.get("sourceAccountCurrencyCode"))+")");
						trxActivity.setTrxAmount(ValueUtils.getValue(resultMap.get("transactionCurrency"), "") + " " + df.format(ValueUtils.getValue(resultMap.get("transactionAmount"), 0)));
					} else {
						trxActivity.setCorpAccount("");
						trxActivity.setTrxAmount("");
					}
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
			
			String masterReportFile = reportFolder + File.separator + "CorporateAdmin" + File.separator + "Financial" + File.separator + "download-transaction" + "-" + locale.getLanguage() + ".jasper";;
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, reportParams, new JREmptyDataSource());
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
			Path destinationFilePath = Paths.get(destinationFile);
			
			//write files
			Files.write(destinationFilePath, bytes);
			
			result.put(ApplicationConstants.FILENAME, destinationFile);
			
			return result;
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}

}
