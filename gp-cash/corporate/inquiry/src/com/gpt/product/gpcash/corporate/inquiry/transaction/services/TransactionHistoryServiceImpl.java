package com.gpt.product.gpcash.corporate.inquiry.transaction.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.inquiry.transaction.bean.TransactionData;
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
public class TransactionHistoryServiceImpl implements TransactionHistoryService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.trxhistory.download.path}")
	private String pathDownload;
	
    @Autowired
    private EAIEngine eaiAdapter;
    
    @Autowired
    private CorporateAccountGroupService corporateAccountGroupService;
    
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private PendingDownloadService pendingDownloadService;
	
	@Value("${gpcash.batch.sot.timeout}")
	private int timeout;

	@Value("${gpcash.batch.sot.batch-size}")
	private int batchSizeThreshold;	
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
    
	@Override
	public Map<String, Object> periodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Date fromDate = (Date)map.get("fromDate");
			Date toDate = (Date)map.get("toDate");
			int currentPage = (int)map.get("currentPage");
			int pageSize = (int)map.get("pageSize");

			String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
						
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
			
			
			AccountModel account = gdm.getCorporateAccount().getAccount();
			String accountNo = account.getAccountNo();
			String accountType = account.getAccountType().getCode();
			
			//CHECK ACCOUNT BELONG TO CORPORATE ID and USER ID
			corporateAccountGroupService.searchAccountByAccountNoForInquiryOnly(corpId,	userCode, accountNo, true);
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", accountNo);
			inputs.put("fromDate", fromDate);
			inputs.put("toDate", toDate);
			inputs.put("currentPage", currentPage);
			inputs.put("pageSize", pageSize);
			inputs.put("accountType", accountType);
			
			Map<String,Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY, inputs);
			
			Map<String,Object> result = new HashMap<>();
			result.put("openingBalance", outputs.get("openingBalance"));
			result.put("endingBalance", outputs.get("endingBalance"));
			result.put("totalDebitAmount", outputs.get("totalDebitAmount"));
			result.put("totalDebitTrx", outputs.get("totalDebitTrx"));
			result.put("totalCreditAmount", outputs.get("totalCreditAmount"));
			result.put("totalCreditTrx", outputs.get("totalCreditTrx"));
			result.put("transactions", outputs.get("transactions"));
			result.put("totalRecord", outputs.get("totalRecord"));
			result.put("totalPage", outputs.get("totalPage"));
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> downloadPeriodicTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Date fromDate = (Date)map.get("fromDate");
			Date toDate = (Date)map.get("toDate");

			String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);	
			String fileFormat = (String) map.get("fileFormat");
			String isZip = (String) map.get("isZip");
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			
			String ext = getDownloadFileExtention(fileFormat);
			
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
			
			AccountModel account = gdm.getCorporateAccount().getAccount();
			
			//CHECK ACCOUNT BELONG TO CORPORATE ID and USER ID
			corporateAccountGroupService.searchAccountByAccountNoForInquiryOnly(corpId,	userCode, account.getAccountNo(), true);
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", account.getAccountNo());
			inputs.put("accountCurrency", account.getCurrency().getCode());
			inputs.put("accountType", account.getAccountType().getCode());
			inputs.put("fromDate", fromDate);
			inputs.put("toDate", toDate);
			inputs.put("fileFormat", fileFormat);
			
			Map<String,Object> outputs = null;
			
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			
			String sourceFile = pathDownload + File.separator + account.getAccountNo().concat("-").concat(downloadRefNo).concat(".").concat(ext);
			
			if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_CSV) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_TXT)) {
				outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD, inputs);
				
				Path sourcePath = Paths.get(sourceFile);
		        Files.write(sourcePath, (byte[]) outputs.get("rawdata"));
		        
			} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_940) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_942)) {
				inputs.put("statementRefNo", downloadRefNo);
				outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD_MT, inputs);
				
				Path sourcePath = Paths.get(sourceFile);
		        Files.write(sourcePath, (byte[]) outputs.get("rawdata"));
		        
			}
			
	        Map<String, Object> result = new HashMap<>();
				        
	        if(ApplicationConstants.YES.equals(isZip)) {
	        	String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".zip");
	        	Helper.addToZipFile(new String[] {sourceFile}, destinationFile, true);
				
				result.put(ApplicationConstants.FILENAME, destinationFile);				
	        } else {
	        	result.put(ApplicationConstants.FILENAME, sourceFile);
	        }

	        return result;		
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private String getDownloadFileExtention(String fileFormat) {
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_CSV)) {
			return "csv";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		}
		
		return "txt";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> downloadPeriodicTransactionMultiAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			
			Date fromDate = (Date)map.get("fromDate");
			Date toDate = (Date)map.get("toDate");
			String fileSetting = (String) map.get("fileSetting");
			String fileFormat = (String) map.get("fileFormat");
			
			String ext = getDownloadFileExtention(fileFormat);
			
			List<String> accountGroupDtlIdList = (List<String>)map.get("accountGroupDtlIdList");			
			if (accountGroupDtlIdList.size()==0)
				throw new BusinessException("GPT-0100049");
			
			// check account no validity first
			List<AccountModel> accountsInfo = new ArrayList<>();
			for (String accountGroupDetailId : accountGroupDtlIdList) {							
				CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
				
				AccountModel account = gdm.getCorporateAccount().getAccount();
				
				//CHECK ACCOUNT BELONG TO CORPORATE ID and USER ID
				corporateAccountGroupService.searchAccountByAccountNoForInquiryOnly(corpId,	userCode, account.getAccountNo(), true);
			
				accountsInfo.add(account);
			}
			
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
        	String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".zip");
				
			// request to eai to retrieve transaction history
        	StringBuffer consolidated = new StringBuffer();
        	List<String> zipFiles = new ArrayList<String>();
			for (AccountModel account : accountsInfo) {
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("accountNo", account.getAccountNo());
				inputs.put("accountCurrency", account.getCurrency().getCode());
				inputs.put("accountType", account.getAccountType().getCode());
				inputs.put("fromDate", fromDate);
				inputs.put("toDate", toDate);
				inputs.put("fileFormat", fileFormat);
				
				Map<String,Object> outputs = null;	
				
				if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_CSV) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_TXT)) {
					outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD, inputs);
				} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_940) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_942)) {
					inputs.put("statementRefNo", downloadRefNo);
					outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD_MT, inputs);
				}
				
				if(outputs != null) {
					if(ApplicationConstants.FILE_SETTING_SEPARATED.equals(fileSetting)) {
						String sourceFile = pathDownload + File.separator + account.getAccountNo().concat("-").concat(downloadRefNo).concat(".").concat(ext);
						Path sourcePath = Paths.get(sourceFile);
				        Files.write(sourcePath, (byte[])outputs.get("rawdata"));
				        
				        zipFiles.add(sourceFile);
					} else {
						consolidated.append(new String((byte[])outputs.get("rawdata")));
					}
				}
							
			}
			
			if(ApplicationConstants.FILE_SETTING_SEPARATED.equals(fileSetting)) {
				if (ValueUtils.hasValue(zipFiles)) {
					String[] sourceFiles = zipFiles.toArray(new String[zipFiles.size()]);
					Helper.addToZipFile(sourceFiles, destinationFile, true);
				}
			} else {
				String sourceFile = pathDownload + File.separator + downloadRefNo.concat(".").concat(ext);
				Path sourcePath = Paths.get(sourceFile);
		        Files.write(sourcePath, consolidated.toString().getBytes());
				
		        //add to zip
		        Helper.addToZipFile(new String[] {sourceFile}, destinationFile, true);
			}
						
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, destinationFile);
						
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> downloadPeriodicTransactionMultiAccountForPDF(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			String userName = idmUserRepo.findOne(userCode).getName();
			
			Date fromDate = (Date)map.get("fromDate");
			Date toDate = (Date)map.get("toDate");
			String fileSetting = (String) map.get("fileSetting");
			String fileFormat = (String) map.get("fileFormat");
			String isZip = (String) map.get("isZip");
			
			String ext = getDownloadFileExtention(fileFormat);
			
			List<String> accountGroupDtlIdList = (List<String>)map.get("accountGroupDtlIdList");			
			if (accountGroupDtlIdList.size()==0)
				throw new BusinessException("GPT-0100049");
			
			// check account no validity first
			List<AccountModel> accountsInfo = new ArrayList<>();
			String corporateName = "";
			String address1 = "", address2 = "", address3 = "";
			String productDescription = "";
			int i = 0;
			for (String accountGroupDetailId : accountGroupDtlIdList) {							
				CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
				
				AccountModel account = gdm.getCorporateAccount().getAccount();
				
				if(i == 0) {
					CorporateModel corporate = gdm.getCorporateAccount().getCorporate();
					corporateName = corporate.getName();
					
					if(corporate.getAddress1() != null)
						address1 = corporate.getAddress1();
					
					if(corporate.getAddress2() != null)
						address2 = corporate.getAddress2();
					
					if(corporate.getAddress3() != null)
						address3 = corporate.getAddress3();
					
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", account.getAccountNo());
					try {
						Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
						productDescription = (String) outputs.get("productDescription");
					} catch (Exception e) {
						logger.error("Error while doAccountInquiry");
					}
				}
				
				//CHECK ACCOUNT BELONG TO CORPORATE ID and USER ID
				corporateAccountGroupService.searchAccountByAccountNoForInquiryOnly(corpId,	userCode, account.getAccountNo(), true);
			
				accountsInfo.add(account);
				i++;
			}
			
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
        	String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".zip");
				
        	List<Map<String, Object>> consolidatedInputList = new ArrayList<>();
        	
        	List<String> zipFiles = new ArrayList<String>();
			for (AccountModel account : accountsInfo) {
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("accountNo", account.getAccountNo());
				inputs.put("accountCurrency", account.getCurrency().getCode());
				inputs.put("accountType", account.getAccountType().getCode());
				inputs.put("fromDate", fromDate);
				inputs.put("toDate", toDate);
				inputs.put("fileFormat", fileFormat);
				
				inputs.put("corporateName", corporateName);
				inputs.put("address1", address1);
				inputs.put("address2", address2);
				inputs.put("address3", address3);
				inputs.put("productDescription", productDescription);
				inputs.put("accountName", account.getAccountName());
				inputs.put("requestReportUserName", userName);
				
				BranchModel accountBranch;
				//jika tidak ada di maintenance maka ambil default
				if(account.getBranch() != null) {
					accountBranch = maintenanceRepo.getBranchRepo().findOne(account.getBranch().getCode());
				} else {
					accountBranch = maintenanceRepo.getBranchRepo().findOne(maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_BRANCH_CODE).getValue());
				}
				
				inputs.put("accountBranchCode", accountBranch.getCode());
				inputs.put("accountBranchName", accountBranch.getName());
				
				if(ApplicationConstants.FILE_SETTING_SEPARATED.equals(fileSetting)) {
					String sourceFile = pathDownload + File.separator + account.getAccountNo().concat("-").concat(downloadRefNo).concat(".").concat(ext);
					
					Map<String, Object> singleInput = new HashMap<>();
					singleInput.put("destinationFile", sourceFile);
					singleInput.put("fileFormat", fileFormat);
					
					consolidatedInputList  = new ArrayList<>();
					consolidatedInputList.add(inputs);
					singleInput.put("consolidatedInputList", consolidatedInputList);
//					eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT, singleInput);
					doTransactionHistoryDownloadPDFMultiAccount(singleInput);
			        
			        zipFiles.add(sourceFile);
				} else {
					consolidatedInputList.add(inputs);
				}				
			}

			Map<String, Object> result = new HashMap<>();
			
			if(ApplicationConstants.FILE_SETTING_SEPARATED.equals(fileSetting)) {
				if (ValueUtils.hasValue(zipFiles)) {
					String[] sourceFiles = zipFiles.toArray(new String[zipFiles.size()]);
					Helper.addToZipFile(sourceFiles, destinationFile, true);
					
					result.put(ApplicationConstants.FILENAME, destinationFile);	
				}
			} else {
				String sourceFile = pathDownload + File.separator + downloadRefNo.concat(".").concat(ext);
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("destinationFile", sourceFile);
				inputs.put("consolidatedInputList", consolidatedInputList);
				inputs.put("fileFormat", fileFormat);
//				eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT, inputs);
				doTransactionHistoryDownloadPDFMultiAccount(inputs);

				if(ApplicationConstants.YES.equals(isZip)) {
					 //add to zip
			        Helper.addToZipFile(new String[] {sourceFile}, destinationFile, true);
					
					result.put(ApplicationConstants.FILENAME, destinationFile);				
		        } else {
		        	result.put(ApplicationConstants.FILENAME, sourceFile);
		        }
				
		       
			}
						
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	public Map<String, Object> doTransactionHistoryDownloadPDFMultiAccount(Map<String, Object> data) throws ApplicationException, BusinessException {
		try {
			Map<String,Object> returnMap = null;	
			String fileFormat = (String) data.get("fileFormat");
			byte[] bytes;
			
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
			DecimalFormat df = new DecimalFormat(moneyFormat); 
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			
			List<Map<String, Object>> consolidatedInputList = (ArrayList<Map<String, Object>>) data.get("consolidatedInputList");
			
			Collection<Map<String, Object>> beanCollection = new ArrayList<>();
			
			int i  = 0;
			Collection<Map<String, Object>> masterReportCollection = new ArrayList<>();
			
			String prevAccountNo = "";
			for(Map<String, Object> input : consolidatedInputList) {
				
				Map<String, Object> recordMap = new HashMap<>();
				recordMap.put("masterReportCounter", i);
				masterReportCollection.add(recordMap);
				
				input.put("currentPage", 0);
				input.put("pageSize", 0);
				
				Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSACTION_HISTORY, input);
				
				String corporateName = (String) input.get("corporateName");
				String accountNo = (String) input.get("accountNo");
				String accountName = (String) input.get("accountName");
				String accountCurrency = (String) input.get("accountCurrency");
				String accountBranchCode = (String) input.get("accountBranchCode");
				String accountBranchName = (String) input.get("accountBranchName");
				
				List<Map<String, Object>> transactions = (ArrayList<Map<String,Object>>)outputs.get("transactions");

				HashMap<String, Object> reportParams = new HashMap<>();
				List<TransactionData> detailDSList = new ArrayList<>();
				
				if (transactions!=null) {
					TransactionData trx = null;
					int record = 1;
					for (Map<String, Object> trxMap : transactions) {
						trx = new TransactionData();
						
						trx.setNo(String.valueOf(record));
						
						BigDecimal debitAmount = (BigDecimal) trxMap.get("debitAmount");
						BigDecimal creditAmount = (BigDecimal) trxMap.get("creditAmount");
						
						String debitAmountStr = "", creditAmountStr = "";
						
						if(!debitAmount.equals(BigDecimal.ZERO)) {
							debitAmountStr = df.format(debitAmount);
						}
						
						if(!creditAmount.equals(BigDecimal.ZERO)) {
							creditAmountStr = df.format(creditAmount);
						}
						
						trx.setPostDate(sdfDate.format((Date) trxMap.get("postDate")));
						trx.setEffectiveDate(sdfDate.format((Date) trxMap.get("effectiveDate")));
						trx.setDebitAmount(debitAmountStr);
						trx.setCreditAmount(creditAmountStr);
						trx.setBalance(df.format((BigDecimal) trxMap.get("balance")));
						trx.setDescription((String) trxMap.get("description"));
						trx.setFileFormat(fileFormat);
						detailDSList.add(trx);
						
						record++;
					}
				}
				
				reportParams.put("bankLogo", bankLogo);
				
				JRBeanCollectionDataSource transactionDataSource= new JRBeanCollectionDataSource(detailDSList);
				reportParams.put("transactionDataSource", transactionDataSource);
				
				
				reportParams.put("fileFormat", fileFormat);
				reportParams.put("corporateName", corporateName);
				reportParams.put("productDescription", input.get("productDescription"));
				reportParams.put("address1", input.get("address1"));
				reportParams.put("address2", input.get("address2"));
				reportParams.put("address3", input.get("address3"));
				reportParams.put("accountNo", accountNo);
				reportParams.put("prevAccountNo", prevAccountNo);
				reportParams.put("accountName", accountName);
				reportParams.put("accountInfo", accountNo.concat(" - ").concat(accountName));
				reportParams.put("accountCurrency", accountCurrency);
				reportParams.put("accountBranch", accountBranchCode.concat(" - ").concat(accountBranchName));
				reportParams.put("periods", sdfDate.format((Date) input.get("fromDate")).concat(" - ").concat(sdfDate.format((Date) input.get("toDate"))));
				reportParams.put("printDate", sdfDateTime.format(new Date()));
				reportParams.put("requestReportUserName", input.get("requestReportUserName"));
				
				reportParams.put("openingBalance", df.format((BigDecimal) outputs.get("openingBalance")));
				reportParams.put("endingBalance", df.format((BigDecimal) outputs.get("endingBalance")));
				reportParams.put("totalDebitAmount", df.format((BigDecimal) outputs.get("totalDebitAmount")));
				reportParams.put("totalDebitTrx", String.valueOf(outputs.get("totalDebitTrx")));
				reportParams.put("totalCreditAmount", df.format((BigDecimal) outputs.get("totalCreditAmount")));
				reportParams.put("totalCreditTrx", String.valueOf(outputs.get("totalCreditTrx")));
				
				beanCollection.add(reportParams);
				
				i++;
			}
			
			Locale locale = LocaleContextHolder.getLocale();
			
			Path destinationFile = Paths.get((String) data.get("destinationFile"));
			
			String masterReportFile = reportFolder + File.separator + "TransactionHistory" + File.separator + "download-transaction-master.jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			String subReportFile = reportFolder + File.separator + "TransactionHistory" + File.separator + "download-transaction" + "-" + locale.getLanguage() + ".jasper";
			JasperReport subReport = (JasperReport) JRLoader.loadObject(new File(subReportFile));
			
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("SUB_REPORT", subReport);
	        parameters.put("DATA",  beanCollection);
	        parameters.put("fileFormat", fileFormat);
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, parameters, new JRBeanCollectionDataSource(masterReportCollection));
			
			if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
				bytes = JasperExportManager.exportReportToPdf(print);
				
				//write files
				Files.write(destinationFile, bytes);
			} else {
                JRXlsExporter xlsExporter = new JRXlsExporter();
                xlsExporter.setExporterInput(new SimpleExporterInput(print));
                xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destinationFile.toFile()));
                SimpleXlsxReportConfiguration xlsReportConfiguration = new SimpleXlsxReportConfiguration();
                xlsReportConfiguration.setOnePagePerSheet(false);
                xlsReportConfiguration.setDetectCellType(true);
                xlsReportConfiguration.setWhitePageBackground(false);
                xlsReportConfiguration.setShowGridLines(false);
                xlsExporter.setConfiguration(xlsReportConfiguration);
                xlsExporter.exportReport();
			}
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);			
			throw new BusinessException("EAI-EAI-00");
		}
	}
    
	@Override
	public Map<String, Object> latestTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			int limit = (Integer)map.get("limit");
			
			String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
			
			String accountNo = gdm.getCorporateAccount().getAccount().getAccountNo();			
			
			//CHECK ACCOUNT BELONG TO CORPORATE ID and USER ID
			corporateAccountGroupService.searchAccountByAccountNoForInquiryOnly(corpId,	userCode, accountNo, true);
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", accountNo);
			inputs.put("limit", limit);
			
			Map<String,Object> outputs = eaiAdapter.invokeService(EAIConstants.LATEST_TRANSACTION, inputs);	
			
			Map<String,Object> result = new HashMap<>();
			result.put("openingBalance", BigDecimal.ZERO);
			result.put("endingBalance", BigDecimal.ZERO);
			result.put("totalDebitAmount", outputs.get("totalDebitAmount"));
			result.put("totalDebitTrx", outputs.get("totalDebitTrx"));
			result.put("totalCreditAmount", outputs.get("totalCreditAmount"));
			result.put("totalCreditTrx", outputs.get("totalCreditTrx"));
			result.put("transactions", outputs.get("transactions"));
			
			return result;			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}    
	
	@Override
	public void executeSOTRequestScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Date requestDate = DateUtils.getCurrentDate();
			
			//requestDate by default is H-1
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(requestDate);
			calFrom.add(Calendar.DATE, -1);
			requestDate = calFrom.getTime();
			
			String[] parameterArr = parameter.split("\\|");
			
			String sotType = parameterArr[0];
			
			if(parameterArr.length > 1) {
				String[] requestDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(requestDateArr[1])){
					requestDate = Helper.DATE_FORMATTER.parse(requestDateArr[1]);
				}
			}
			
			boolean isNew = false;
			if(ApplicationConstants.SOT_TYPE_NEW_CORPORATE_ACCOUNT.equals(sotType)) {
				isNew = true;
			}
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("requestDate", requestDate);
			inputs.put("isNew", isNew);
			inputs.put("maxLenghtAccountRegular", ApplicationConstants.MAX_LENGTH_ACCOUNT_REG);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeSOTRequestScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.SOT_REQUEST, inputs);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}  
	
	@Override
	public void executeSOTResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Date requestDate = DateUtils.getCurrentDate();
			String filename = "";
			
			//requestDate by default is H-1
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(requestDate);
			calFrom.add(Calendar.DATE, -1);
			requestDate = calFrom.getTime();
			
			
			String[] parameterArr = parameter.split("\\|");
			
			String sotType = parameterArr[0];
			
			
			if(parameterArr.length > 1) {
				String[] requestDateArr = parameterArr[1].split("\\=");
				if(ValueUtils.hasValue(requestDateArr[0])) {
						if(ValueUtils.hasValue(requestDateArr[1])){
							if("filename".equals(requestDateArr[0])) {
								filename = requestDateArr[1];
							}else {
								requestDate = Helper.DATE_FORMATTER.parse(requestDateArr[1]);
							}
						}					
				}
				
			}
			
			boolean isNew = false;
			if(ApplicationConstants.SOT_TYPE_NEW_CORPORATE_ACCOUNT.equals(sotType)) {
				isNew = true;
			}
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("filename", filename);
			inputs.put("requestDate", requestDate);
			inputs.put("isNew", isNew);
			inputs.put("timeout", timeout);
			inputs.put("batchSizeThreshold", batchSizeThreshold);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeSOTResponseScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.SOT_RESPONSE, inputs);
			
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
			
			Map<String, Object> returnMap = new HashMap<>();
			
			String fileFormat = (String) map.get("fileFormat");
			String searchType = (String) map.get("searchType");
			
			if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
				if (searchType.equals("single")) {
					List<String> accountGroupDtlIdList = new ArrayList<>();
					accountGroupDtlIdList.add((String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
					map.put("accountGroupDtlIdList", accountGroupDtlIdList);
				}
				returnMap = downloadPeriodicTransactionMultiAccountForPDF(map);
			} else {
				if (searchType.equals("multi")) {
					returnMap = downloadPeriodicTransactionMultiAccount(map);
				} else  {
					returnMap = downloadPeriodicTransaction(map);
				}
			}
			
			String destinationFile = (String) returnMap.get(ApplicationConstants.FILENAME);
			
			pendingDownloadService.updateToComplete(downloadId, destinationFile);
			
		} catch (BusinessException e) {
			pendingDownloadService.updateToNewRequest(downloadId);
			logger.error("Failed to generateReport with downloadId " + downloadId + " " + e.getMessage(),e);
		} catch (Exception e) {
			pendingDownloadService.updateToNewRequest(downloadId);
			logger.error("Failed to generateReport with downloadId " + downloadId + " " + e.getMessage(),e);
		}
		
		
	}

	@Override
	public Map<String, Object> downloadPending(Map<String, Object> map, String requestBy) throws ApplicationException {
		try {
			String downloadId = (String) map.get("downloadId");
			return pendingDownloadService.downloadReport(downloadId, requestBy);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> deletePendingDownload(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			List<String> pendingDownloadList = (List<String>) map.get("downloadIdList");
			String deletedBy = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			return pendingDownloadService.deletePendingDownload(pendingDownloadList, deletedBy);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}  
    
}
