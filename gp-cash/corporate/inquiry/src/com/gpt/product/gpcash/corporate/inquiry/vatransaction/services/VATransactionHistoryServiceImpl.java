package com.gpt.product.gpcash.corporate.inquiry.vatransaction.services;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class VATransactionHistoryServiceImpl implements VATransactionHistoryService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.vahistory.download.path}")
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
	
	@Value("${gpcash.batch.sot.timeout}")
	private int timeout;

	@Value("${gpcash.batch.sot.batch-size}")
	private int batchSizeThreshold;	
    
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
			
			Map<String,Object> outputs = eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY, inputs);
			
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
				outputs = eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD, inputs);
				
				Path sourcePath = Paths.get(sourceFile);
		        Files.write(sourcePath, (byte[]) outputs.get("rawdata"));
		        
			} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_940) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_942)) {
				inputs.put("statementRefNo", downloadRefNo);
				outputs = eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD_MT, inputs);
				
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
					outputs = eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD, inputs);
				} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_940) || fileFormat.equals(ApplicationConstants.FILE_FORMAT_MT_942)) {
					inputs.put("statementRefNo", downloadRefNo);
					outputs = eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD_MT, inputs);
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
					eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT, singleInput);
			        
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
				eaiAdapter.invokeService(EAIConstants.VA_TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT, inputs);

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
	public void executeVASOTRequestScheduler(String parameter) throws ApplicationException, BusinessException {
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
				logger.debug("executeVASOTRequestScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.VA_SOT_REQUEST, inputs);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}  
	
	@Override
	public void executeVASOTResponseScheduler(String parameter) throws ApplicationException, BusinessException {
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
				logger.debug("executeVASOTResponseScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.VA_SOT_RESPONSE, inputs);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}  
    
}
