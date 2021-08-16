package com.gpt.product.gpcash.corporate.transaction.report.charging.services;

import java.io.File;
import java.math.BigDecimal;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.repository.GlobalTransactionRepository;
import com.gpt.product.gpcash.corporate.transaction.report.charging.bean.ChargingBean;
import com.gpt.product.gpcash.pendingdownload.services.PendingDownloadService;
import com.gpt.product.gpcash.service.repository.ServiceRepository;

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
public class ChargingReportServiceImpl implements ChargingReportService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.temp.download.path}")
	private String pathDownload;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private CorporateRepository corporateRepo;
	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	@Autowired
	private GlobalTransactionRepository globalTransactionRepo;
	
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
			String searchMenuCode = (String) map.get("searchMenuCode");
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
			
			List<Object[]> services = serviceRepo.findServiceByMenuCode(searchMenuCode);
			
			int i  = 0;
			int grantTotalVolume = 0;
			BigDecimal grantTotalFeeAmount = BigDecimal.ZERO;
			for(Object[] service : services) {
				Map<String, Object> groupByServiceInfo = getReportParamsByService((String) service[0], (String) service[1], 
						fileFormat, startDateSql, endDateSql);

				if(((Integer)groupByServiceInfo.get("totalVolume")) > 0) {
					Map<String, Object> recordMap = new HashMap<>();
					recordMap.put("masterReportCounter", i);
					masterReportCollection.add(recordMap);
					
					beanCollection.add(groupByServiceInfo);
					
					int totalVolume = (Integer) groupByServiceInfo.get("totalVolume");
					grantTotalVolume = grantTotalVolume + totalVolume;
					
					
					BigDecimal totalFee = (BigDecimal) groupByServiceInfo.get("totalFeeAmount");
					grantTotalFeeAmount = grantTotalFeeAmount.add(totalFee);
					
					i++;
				}
			}
			
			String generateInfo = "generated by ".concat(requestBy).concat(" on ").concat(sdfDateTime.format(DateUtils.getCurrentTimestamp()));
			String periods = "Order Period ".concat(sdfDate.format(fromDate)).concat(" - ").concat(sdfDate.format(toDate));
			
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("DATA",  beanCollection);
	        parameters.put("generateInfo", generateInfo);
	        parameters.put("periods", periods);
	        parameters.put("grantTotalVolume", String.valueOf(grantTotalVolume));
	        parameters.put("grantTotalFeeAmount", String.valueOf(grantTotalFeeAmount));
	        
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
	
	private Map<String, Object> getReportParamsByService(String serviceCode, String serviceName, 
			String fileFormat, java.sql.Date startDate, java.sql.Date endDate) throws Exception{
		HashMap<String, Object> reportParams = new HashMap<>();
		
		List<String> corporates = globalTransactionRepo.findCorporatesByService(serviceCode, startDate, endDate);
		
		String equivalentCurrency = sysParamRepo.getOne(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
		
		List<ChargingBean> detailDSList = new ArrayList<>();
		int totalVolume = 0;
		BigDecimal totalFeeAmount = BigDecimal.ZERO;
		if(corporates.size() > 0) {
			ChargingBean chargingBean = null;
			int record = 1;
			for (String corporateId : corporates) {
				chargingBean = new ChargingBean();
				
				chargingBean.setFileFormat(fileFormat);
				chargingBean.setNo(String.valueOf(record));
				
				if(record == 1) {
					chargingBean.setService(serviceName);
				} else {
					chargingBean.setService(ApplicationConstants.EMPTY_STRING);
				}
				
				chargingBean.setCurrency(equivalentCurrency);
				
				CorporateModel corporate = corporateRepo.findOne(corporateId);
				chargingBean.setCorporate(corporate.getId().concat(" - ").concat(corporate.getName()));
				
				int volume = globalTransactionRepo.countTotalChargeByCorporateId(corporateId, serviceCode, startDate, endDate);
				chargingBean.setVolume(String.valueOf(volume));
				totalVolume = totalVolume + volume;
				chargingBean.setTotalVolume(String.valueOf(totalVolume));
				
				BigDecimal feeAmount = globalTransactionRepo.sumTotalChargeByCorporateId(corporateId, serviceCode, startDate, endDate);
				chargingBean.setAmount(String.valueOf(feeAmount));
				totalFeeAmount = totalFeeAmount.add(feeAmount);
				chargingBean.setTotalFeeAmount(totalFeeAmount.toPlainString());
				
				chargingBean.setTotalCorporate(String.valueOf(corporates.size()));
				
				detailDSList.add(chargingBean);
				record++;
			}
		}
		
		JRBeanCollectionDataSource detailDataSource= new JRBeanCollectionDataSource(detailDSList);
		reportParams.put("detailDataSource", detailDataSource);
		reportParams.put("totalVolume", totalVolume);
		reportParams.put("totalFeeAmount", totalFeeAmount);
		
		//-----------------------
		
		return reportParams;
	}
	
	private void generateReport(Collection<Map<String, Object>> masterReportCollection,
			Map<String, Object> parameters,
			String destinationFile,
			String fileFormat) throws Exception {
		
		String masterReportFile = reportFolder + File.separator + "BankReport" + File.separator + "Charging" + File.separator + "charging-master.jasper";
		JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
		
		String subReportFile = reportFolder + File.separator + "BankReport" + File.separator + "Charging" + File.separator +"charging" + "-" + locale.getLanguage() + ".jasper";
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
	
	@Override
	public Map<String, Object> searchMenu() throws ApplicationException {
		Map<String, Object> result = new HashMap<>();
		try {
			List<Object[]> datas = serviceRepo.findMenuByServiceType(ApplicationConstants.SERVICE_TYPE_TRX);
			
			List<Map<String, String>> menuMapList = new ArrayList<>();
			for(Object[] data : datas) {
				Map<String, String> menuMap = new HashMap<>();
				menuMap.put("menuCode", (String) data[0]);
				menuMap.put("menuName", (String) data[1]);
				menuMapList.add(menuMap);
			}
			result.put("result", menuMapList);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return result;
	}
}