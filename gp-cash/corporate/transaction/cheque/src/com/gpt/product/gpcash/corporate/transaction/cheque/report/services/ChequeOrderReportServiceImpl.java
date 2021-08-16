package com.gpt.product.gpcash.corporate.transaction.cheque.report.services;

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
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.branch.repository.BranchRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transaction.cheque.constants.ChequeConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderModel;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.repository.ChequeOrderRepository;
import com.gpt.product.gpcash.corporate.transaction.cheque.report.bean.ChequeOrderBean;
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
public class ChequeOrderReportServiceImpl implements ChequeOrderReportService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.temp.download.path}")
	private String pathDownload;
	
	@Autowired
	private ChequeOrderRepository chequeOrderRepo;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private BranchRepository branchRepo;
	
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
			String fileName = (String) map.get(ApplicationConstants.FILENAME);
			
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");

			Collection<Map<String, Object>> beanCollection = new ArrayList<>();
			Collection<Map<String, Object>> masterReportCollection = new ArrayList<>();
			
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
			
			java.sql.Date startDateSql = new java.sql.Date(startDate.getTimeInMillis());
			java.sql.Date endDateSql = new java.sql.Date(endDate.getTimeInMillis());
			
			List<String> branches;
			
			List<String> statusList = new ArrayList<>(5);
			
			boolean isStatusCodeEqExpired = false;
			
			if(ValueUtils.hasValue(map.get("statusCode")) && !ChequeConstants.CHQ_STS_EXPIRED.equals(map.get("statusCode"))) {
				statusList.add((String) map.get("statusCode"));
			} else {
				if(ChequeConstants.CHQ_STS_EXPIRED.equals(map.get("statusCode"))) {
					isStatusCodeEqExpired = true;
					statusList.add(ChequeConstants.CHQ_STS_NEW_REQUEST);
				} else {
					statusList.add(ChequeConstants.CHQ_STS_NEW_REQUEST);
					statusList.add(ChequeConstants.CHQ_STS_READY);
					statusList.add(ChequeConstants.CHQ_STS_PICKED_UP);
					statusList.add(ChequeConstants.CHQ_STS_DECLINED);
				}
			}
			
			if(ValueUtils.hasValue(map.get("branchCode"))) {
				branches = chequeOrderRepo.findChequeBranch((String) map.get("branchCode"), statusList, startDateSql,endDateSql);
			} else {
				branches = chequeOrderRepo.findChequeAllBranch(statusList, startDateSql,endDateSql);
			}
			
			
			int i  = 0;
			int grantTotalChequeOrder = 0;
			int grantTotalNewRequest = 0;
			int grantTotalReady = 0;
			int grantTotalDecline = 0;
			int grantTotalPicked = 0;
			int grantTotalExpired = 0;
			for(String branchStr : branches) {
				BranchModel branch = branchRepo.findOne(branchStr);
				
				Map<String, Object> recordMap = new HashMap<>();
				recordMap.put("masterReportCounter", i);
				masterReportCollection.add(recordMap);
				
				Map<String, Object> groupByBranchInfo = getReportParamsByBranch(branch, statusList, isStatusCodeEqExpired, fileFormat, startDateSql, endDateSql);
				beanCollection.add(groupByBranchInfo);
				
				grantTotalChequeOrder = grantTotalChequeOrder + (Integer) groupByBranchInfo.get("totalChequeOrder");
				grantTotalNewRequest = grantTotalNewRequest + (Integer) groupByBranchInfo.get("totalNewRequest");
				grantTotalReady = grantTotalReady + (Integer) groupByBranchInfo.get("totalReady");
				grantTotalDecline = grantTotalDecline + (Integer) groupByBranchInfo.get("totalDecline");
				grantTotalPicked = grantTotalPicked + (Integer) groupByBranchInfo.get("totalPicked");
				grantTotalExpired = grantTotalExpired + (Integer) groupByBranchInfo.get("totalExpired");
				
				i++;
			}
			
			String generateInfo = "generated by ".concat(requestBy).concat(" on ").concat(sdfDateTime.format(DateUtils.getCurrentTimestamp()));
			String periods = "Order Period ".concat(sdfDate.format(fromDate)).concat(" - ").concat(sdfDate.format(toDate));
			
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("DATA",  beanCollection);
	        parameters.put("generateInfo", generateInfo);
	        parameters.put("periods", periods);
	        parameters.put("grantTotalChequeOrder", String.valueOf(grantTotalChequeOrder));
	        parameters.put("grantTotalNewRequest", String.valueOf(grantTotalNewRequest));
	        parameters.put("grantTotalReady", String.valueOf(grantTotalReady));
	        parameters.put("grantTotalDecline", String.valueOf(grantTotalDecline));
	        parameters.put("grantTotalPicked", String.valueOf(grantTotalPicked));
	        parameters.put("grantTotalExpired", String.valueOf(grantTotalExpired));
	        
			String destinationFile = pathDownload + File.separator + fileName;
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
	
	private Map<String, Object> getReportParamsByBranch(BranchModel branch, List<String> statusList, boolean isStatusCodeEqExpired, String fileFormat, java.sql.Date startDate,
			java.sql.Date endDate) throws Exception{
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
		SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		
		HashMap<String, Object> reportParams = new HashMap<>();
		
		String branchInfo = branch.getCode().concat(" - ").concat(branch.getName());
		reportParams.put("branchInfo", branchInfo);
		
		Date currentDate = DateUtils.getCurrentDate();
		reportParams.put("currentDate", sdfDate.format(currentDate));
		
		List<ChequeOrderModel> chequeOrders;
		
		if(isStatusCodeEqExpired) {
			chequeOrders = chequeOrderRepo.findChequeOrderByBranchForExpired(branch.getCode(), statusList, startDate, endDate, DateUtils.getCurrentDate());
		} else {
			chequeOrders = chequeOrderRepo.findChequeOrderByBranch(branch.getCode(), statusList, startDate, endDate);
		}
		
		List<ChequeOrderBean> detailDSList = new ArrayList<>();
		int totalChequeOrder = 0;
		int totalNewRequest = 0;
		int totalReady = 0;
		int totalDecline = 0;
		int totalPicked = 0;
		int totalExpired = 0;
		if(chequeOrders.size() > 0) {
			ChequeOrderBean chequeBean = null;
			int record = 1;
			for (ChequeOrderModel chequeOrder : chequeOrders) {
				chequeBean = new ChequeOrderBean();
				
				chequeBean.setFileFormat(fileFormat);
				chequeBean.setNo(String.valueOf(record));
				chequeBean.setOrderNo(chequeOrder.getOrderNo());
				chequeBean.setOrderDateTime(sdfDateTime.format(chequeOrder.getCreatedDate()));
				
				CorporateModel corporate = chequeOrder.getCorporate();
				chequeBean.setRequestBy(corporate.getId().concat(" - ").concat(corporate.getName()));
				chequeBean.setPickupSchedule(sdfDateTime.format(chequeOrder.getInstructionDate()));
				
				String status = chequeOrder.getStatus();
				
				if(chequeOrder.getStatus().equals(ChequeConstants.CHQ_STS_NEW_REQUEST)) {
					//if status is new request then check if already expired or not
					if(currentDate.compareTo(chequeOrder.getInstructionDate()) > 0) {
						status = ChequeConstants.CHQ_STS_EXPIRED;
						totalExpired++;
					} else {
						totalNewRequest++;
					}
				} else if (chequeOrder.getStatus().equals(ChequeConstants.CHQ_STS_READY)) {
					totalReady++;
				} else if (chequeOrder.getStatus().equals(ChequeConstants.CHQ_STS_DECLINED)) {
					totalDecline++;
				} else if (chequeOrder.getStatus().equals(ChequeConstants.CHQ_STS_PICKED_UP)) {
					totalPicked++;
				}
				
				chequeBean.setStatus(message.getMessage(status, null, status, locale));

				detailDSList.add(chequeBean);
				record++;
				totalChequeOrder++;
			}
		}
		
		JRBeanCollectionDataSource detailDataSource= new JRBeanCollectionDataSource(detailDSList);
		reportParams.put("detailDataSource", detailDataSource);
		reportParams.put("totalChequeOrder", totalChequeOrder);
		reportParams.put("totalNewRequest", totalNewRequest);
		reportParams.put("totalReady", totalReady);
		reportParams.put("totalDecline", totalDecline);
		reportParams.put("totalPicked", totalPicked);
		reportParams.put("totalExpired", totalExpired);
		
		//-----------------------
		
		return reportParams;
	}
	
	private void generateReport(Collection<Map<String, Object>> masterReportCollection,
			Map<String, Object> parameters,
			String destinationFile,
			String fileFormat) throws Exception {
		
		String masterReportFile = reportFolder + File.separator + "BankReport" + File.separator + "ChequeOrder" + File.separator + "cheque-order-master.jasper";
		JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
		
		String subReportFile = reportFolder + File.separator + "BankReport" + File.separator + "ChequeOrder" + File.separator +"cheque-order" + "-" + locale.getLanguage() + ".jasper";
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
}