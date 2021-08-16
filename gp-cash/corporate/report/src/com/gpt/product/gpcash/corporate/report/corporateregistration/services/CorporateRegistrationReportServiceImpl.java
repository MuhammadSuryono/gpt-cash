package com.gpt.product.gpcash.corporate.report.corporateregistration.services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.approvalmap.model.ApprovalMapModel;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.report.corporateregistration.bean.CorporateUserBean;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.pendingdownload.services.PendingDownloadService;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateRegistrationReportServiceImpl implements CorporateRegistrationReportService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.temp.download.path}")
	private String pathDownload;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private PendingDownloadService pendingDownloadService;
	
	Locale locale = LocaleContextHolder.getLocale();
	
	@Override
	public Map<String, Object> downloadReport(Map<String, Object> map, String requestBy) throws ApplicationException {
		try {
			String downloadId = (String) map.get("downloadId");
			return pendingDownloadService.downloadReport(downloadId, requestBy);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	@Override
	public void doGenerateReport(Map<String, Object> map, String requestBy) throws ApplicationException, BusinessException {
		String downloadId = (String) map.get("downloadId");
		
		try {			
			
			//update report to in progress
			pendingDownloadService.updateToInProgress(downloadId);
			//-----------------------------
					
			Date fromDate = (Date) map.get("fromDate");
			Date toDate = (Date) map.get("toDate");
			String fileFormat = (String) map.get("fileFormat");
			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
			
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");

			Collection<Map<String, Object>> beanCollection = new ArrayList<>();
			Collection<Map<String, Object>> masterReportCollection = new ArrayList<>();
			
			int totalCorporate = 0;
			int totalActive = 0;
			int totalInactive = 0;
			if(ValueUtils.hasValue(corporateId)) {
				Map<String, Object> recordMap = new HashMap<>();
				recordMap.put("masterReportCounter", 0);
				masterReportCollection.add(recordMap);
				
				CorporateModel corporate = corporateUtilsRepo.getCorporateRepo().findOne(corporateId);
				Map<String, Object> corporateInfo = getReportParamsByCorporate(corporate, fileFormat);
				beanCollection.add(corporateInfo);
				
				fromDate = DateUtils.getCurrentDate();
				toDate = DateUtils.getCurrentDate();
				
				totalCorporate= 1;
				
				if(ApplicationConstants.ACTIVE.equals(corporateInfo.get("corporateStatus"))) {
					totalActive++;
				} else {
					totalInactive++;
				}
			} else {
				Calendar startDate = DateUtils.getEarliestDate(fromDate);
				startDate.set(Calendar.HOUR_OF_DAY, 0);
				startDate.set(Calendar.MINUTE, 0);
				startDate.set(Calendar.SECOND, 0);
				startDate.set(Calendar.MILLISECOND, 0);
				
				
				Calendar endDate = DateUtils.getNextEarliestDate(toDate);
				endDate.set(Calendar.HOUR_OF_DAY, 0);
				endDate.set(Calendar.MINUTE, 0);
				endDate.set(Calendar.SECOND, 0);
				endDate.set(Calendar.MILLISECOND, 0);
				
				List<CorporateModel> corporates = corporateUtilsRepo
						.getCorporateRepo().findByDateBetween(new java.sql.Date(startDate.getTimeInMillis()), 
								new java.sql.Date(endDate.getTimeInMillis()));
				
				int i  = 0;
				for(CorporateModel corporate : corporates) {
					Map<String, Object> recordMap = new HashMap<>();
					recordMap.put("masterReportCounter", i);
					masterReportCollection.add(recordMap);
					
					Map<String, Object> corporateInfo = getReportParamsByCorporate(corporate, fileFormat);
					beanCollection.add(corporateInfo);
					
					if(ApplicationConstants.ACTIVE.equals(corporateInfo.get("corporateStatus"))) {
						totalActive++;
					} else {
						totalInactive++;
					}
					
					i++;
				}
				
				totalCorporate = corporates.size();
			}
			
			String generateInfo = "generated by ".concat(requestBy).concat(" on ").concat(sdfDateTime.format(DateUtils.getCurrentTimestamp()));
			String periods = "Period ".concat(sdfDate.format(fromDate)).concat(" - ").concat(sdfDate.format(toDate));
			
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("DATA",  beanCollection);
	        parameters.put("generateInfo", generateInfo);
	        parameters.put("periods", periods);
	        parameters.put("totalCorporate", String.valueOf(totalCorporate));
	        parameters.put("totalActive", String.valueOf(totalActive));
	        parameters.put("totalInactive", String.valueOf(totalInactive));
	        
	        //download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".").concat(getDownloadFileExtention(fileFormat));
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n");
				logger.debug("totalCorporate : " + totalCorporate);
				logger.debug("totalActive : " + totalActive);
				logger.debug("totalInactive : " + totalInactive);
				logger.debug("destinationFile : " + destinationFile);
				logger.debug("\n\n\n");
			}
			
			generateReport(masterReportCollection, parameters, destinationFile, fileFormat);
					
			pendingDownloadService.updateToComplete(downloadId, destinationFile);
			
		} catch (BusinessException e) {
			pendingDownloadService.updateToNewRequest(downloadId);
			logger.error("Failed to generateReport with downloadId " + downloadId + " " + e.getMessage(),e);
		} catch (Exception e) {
			pendingDownloadService.updateToNewRequest(downloadId);
			logger.error("Failed to generateReport with downloadId " + downloadId + " " + e.getMessage(),e);
		}
	}
	
	private Map<String, Object> getReportParamsByCorporate(CorporateModel corporate, String fileFormat) throws Exception{
		SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		
		HashMap<String, Object> reportParams = new HashMap<>();
		
		String corporateInfo = corporate.getId().concat(" - ").concat(corporate.getName());
		reportParams.put("corporateInfo", corporateInfo);
		
		reportParams.put("registrationDate", sdfDateTime.format(corporate.getCreatedDate()));
		
		BranchModel branch = corporate.getBranch();
		String branchInfo = ApplicationConstants.EMPTY_STRING;
		if(branch != null) {
			branchInfo = branch.getCode().concat(" - ").concat(branch.getName());
		}
		reportParams.put("branchInfo", branchInfo);
		
		String corporateStatus = ApplicationConstants.ACTIVE;
		if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
			corporateStatus = ApplicationConstants.INACTIVE;
		}
		corporateStatus = message.getMessage(corporateStatus, null, corporateStatus, locale);
		reportParams.put("corporateStatus", corporateStatus);
		
		//corporate user info
		List<CorporateUserModel> users = corporateUtilsRepo.getCorporateUserRepo().findUserByCorporate(corporate.getId());
		String totalUser = String.valueOf(users.size());
		reportParams.put("totalUser", totalUser);
		
		List<CorporateUserBean> detailDSList = new ArrayList<>();
		if(users.size() > 0) {
			CorporateUserBean userBean = null;
			int record = 1;
			for (CorporateUserModel user : users) {
				userBean = new CorporateUserBean();
				
				userBean.setFileFormat(fileFormat);
				userBean.setNo(String.valueOf(record));
				userBean.setUserId(user.getUserId());
				
				IDMUserModel idmUser = user.getUser();
				userBean.setName(idmUser.getName());
				
				ApprovalMapModel approvalMap = user.getApprovalMap();
				WorkflowRoleModel wfRole = approvalMap.getWorkflowRole();
				CorporateUserGroupModel userGroup = user.getCorporateUserGroup();
				AuthorizedLimitSchemeModel als = user.getAuthorizedLimit();
				
				userBean.setRole(wfRole.getName());
				userBean.setGroup(ValueUtils.getValue(userGroup.getName(), ApplicationConstants.DASH));
				
				if(als != null) {
					userBean.setLevel(als.getApprovalLevelAlias());
				} else {
					userBean.setLevel(ApplicationConstants.DASH);
				}
				
				userBean.setRegisterDate(sdfDateTime.format(user.getCreatedDate()));
				detailDSList.add(userBean);
				
				record++;
			}
		}
		
		JRBeanCollectionDataSource userDataSource= new JRBeanCollectionDataSource(detailDSList);
		reportParams.put("userDataSource", userDataSource);
		
		//-----------------------
		
		return reportParams;
	}
	
	private void generateReport(Collection<Map<String, Object>> masterReportCollection,
			Map<String, Object> parameters,
			String destinationFile,
			String fileFormat) throws Exception {
		
		String masterReportFile = reportFolder + File.separator + "BankReport" + File.separator + "CorporateRegistration" + File.separator + "corporate-registration-master.jasper";
		JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
		
		String subReportFile = reportFolder + File.separator + "BankReport" + File.separator + "CorporateRegistration" + File.separator +"corporate-registration" + "-" + locale.getLanguage() + ".jasper";
		JasperReport subReport = (JasperReport) JRLoader.loadObject(new File(subReportFile));
		
		//put subReport and fileFormat
		parameters.put("SUB_REPORT", subReport);
		parameters.put("fileFormat", fileFormat);
		
		JasperPrint print = JasperFillManager.fillReport(masterReport, parameters, new JRBeanCollectionDataSource(masterReportCollection));
		
		
		Path destinationFilePath = Paths.get(destinationFile);
		
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
			
			//write files
			Files.write(destinationFilePath, bytes);
		} else {
            JRXlsExporter xlsExporter = new JRXlsExporter();
            xlsExporter.setExporterInput(new SimpleExporterInput(print));
            xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destinationFilePath.toFile()));
            SimpleXlsxReportConfiguration xlsReportConfiguration = new SimpleXlsxReportConfiguration();
            xlsReportConfiguration.setOnePagePerSheet(false);
            xlsReportConfiguration.setDetectCellType(true);
            xlsReportConfiguration.setWhitePageBackground(false);
            xlsReportConfiguration.setShowGridLines(false);
            xlsExporter.setConfiguration(xlsReportConfiguration);
            xlsExporter.exportReport();
		}
	}

	private String getDownloadFileExtention(String fileFormat) {
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		}
		
		return "txt";
	}
}
